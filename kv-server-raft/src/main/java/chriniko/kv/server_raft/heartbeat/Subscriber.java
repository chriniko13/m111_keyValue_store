package chriniko.kv.server_raft.heartbeat;


import chriniko.kv.protocol.ProtocolConstants;
import chriniko.kv.server_raft.infra.HealthNodeState;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

public final class Subscriber implements ScheduledChannelOperation {

    private final ByteBuffer buffer = ByteBuffer.allocateDirect(ProtocolConstants.BYTES_TO_ALLOCATE_PER_BUFFER);

    private final String id;
    private final ScheduledExecutorService scheduler;

    private final NetworkInterface networkInterface;
    private final InetSocketAddress hostAddress;

    private final InetAddress group;

    private final ConcurrentMap<String, Pulse> lastPulsesByServerId;

    public Subscriber(final String id, final String ip, final String interfaceName, final int port, final int poolSize) {
        if (id.isEmpty() && ip.isEmpty() || interfaceName.isEmpty()) {
            throw new IllegalArgumentException("required id, ip and interfaceName");
        }

        this.id = id;
        this.scheduler = Executors.newScheduledThreadPool(poolSize);
        this.lastPulsesByServerId = new ConcurrentHashMap<>();

        this.hostAddress = new InetSocketAddress(port);

        try {
            this.networkInterface = NetworkInterface.getByName(interfaceName);
            this.group = InetAddress.getByName(ip);
        } catch (SocketException | UnknownHostException e) {
            throw new RuntimeException("unable to start broadcaster", e);
        }
    }

    @Override
    public ScheduledExecutorService getService() {
        return this.scheduler;
    }

    public void run() {
        try (final DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET); final Selector selector = Selector.open()) {

            System.out.printf("Starting subscriber %s", id);

            initChannel(channel, selector);
            doSchedule(channel);

            while (!Thread.currentThread().isInterrupted()) {
                if (selector.isOpen()) {
                    final int numKeys = selector.select();
                    if (numKeys > 0) {
                        handleKeys(channel, selector.selectedKeys());
                    }
                } else {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("unable to run subscriber", e);
        } finally {
            this.scheduler.shutdownNow();
        }
    }

    private void initChannel(final DatagramChannel channel, final Selector selector) throws IOException {

        channel.configureBlocking(false);
        channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);

        channel.bind(this.hostAddress);
        channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, this.networkInterface);

        channel.join(this.group, this.networkInterface);

        channel.register(selector, SelectionKey.OP_READ);
    }


    private void handleKeys(final DatagramChannel channel, final Set<SelectionKey> keys) throws IOException {

        final Iterator<SelectionKey> iterator = keys.iterator();
        while (iterator.hasNext()) {

            final SelectionKey key = iterator.next();
            try {

                if (key.isValid() && key.isReadable()) {

                    buffer.clear();
                    final SocketAddress address = channel.receive(buffer);
                    if (address != null) {

                        //System.out.println("received pulse from " + address);

                        // Note: flip the byte buffer so that we can read from the start correctly.
                        buffer.flip();

                        byte[] dest = new byte[buffer.limit()];
                        for (int i=buffer.position(); i<buffer.limit(); i++) {
                            dest[i] = buffer.get(i);
                        }
                        String serializedInfoReceived = new String(dest);

                        String[] splittedSerializedInfoReceived = serializedInfoReceived.split(ProtocolConstants.INFO_SEP);

                        String serverId = splittedSerializedInfoReceived[0];
                        String serverHealthStateStr = splittedSerializedInfoReceived[1];
                        HealthNodeState healthNodeState = HealthNodeState.valueOf(serverHealthStateStr);

                        //System.out.println("serializedInfoReceived: " + serializedInfoReceived);


                        final Pulse pulse = new Pulse(serverId, healthNodeState);
                        lastPulsesByServerId.put(pulse.getServerId(), pulse);
                    }

                } else {
                    throw new UnsupportedOperationException("key not valid.");
                }
            } finally {
                iterator.remove();
            }
        }
    }

    private void doSchedule(final DatagramChannel channel) {

        doSchedule(channel, new Runnable() {
            public void run() {

                System.out.println();

                Subscriber.this.lastPulsesByServerId.forEach((id, pulse) -> {

                    if (id.equals(Subscriber.this.id)) {
                        System.out.println("SAME: received the heartbeat for myself " + Subscriber.this.id);
                        //return;
                    }

                    if (pulse.isDead(Constants.Schedule.DOWNTIME_TOLERANCE_DEAD_SERVICE_IN_MILLISECONDS)) {

                        System.out.printf("FATAL   : %s removed  --- pulse info: %s%n", id, pulse);

                        Subscriber.this.lastPulsesByServerId.remove(id);

                    } else if (!pulse.isValid(Constants.Schedule.DOWNTIME_TOLERANCE_IN_MILLISECONDS)) {

                        System.out.printf("WARNING : %s is down  --- pulse info: %s%n", id, pulse);

                    } else {
                        System.out.printf("OK      : %s is up  --- pulse info: %s%n", id, pulse);
                    }
                });

            }
        }, 0L, Constants.Schedule.PULSE_DELAY_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
    }
}
