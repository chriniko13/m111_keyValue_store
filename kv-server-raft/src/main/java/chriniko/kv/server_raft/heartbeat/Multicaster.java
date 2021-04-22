package chriniko.kv.server_raft.heartbeat;

import chriniko.kv.protocol.ProtocolConstants;
import chriniko.kv.server_raft.infra.HealthNodeState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Multicaster implements ScheduledChannelOperation {

    private final ByteBuffer buffer = ByteBuffer.allocateDirect(ProtocolConstants.BYTES_TO_ALLOCATE_PER_BUFFER);

    private final String id;
    private final ScheduledExecutorService scheduler;

    private final NetworkInterface networkInterface;
    private final InetSocketAddress multicastGroup;

    public Multicaster(final String id, final String ip, final String interfaceName, final int port, final int poolSize) {
        if (id.isEmpty() || ip.isEmpty() || interfaceName.isEmpty()) {
            throw new IllegalArgumentException("required id, ip and interfaceName");
        }

        this.id = id;
        this.scheduler = Executors.newScheduledThreadPool(poolSize);

        this.multicastGroup = new InetSocketAddress(ip, port);

        try {
            this.networkInterface = NetworkInterface.getByName(interfaceName);
        } catch (SocketException e) {
            throw new RuntimeException("unable to start broadcaster", e);
        }
    }

    @Override
    public ScheduledExecutorService getService() {
        return this.scheduler;
    }

    public void run(final CountDownLatch endLatch) {

        try (final DatagramChannel channel = DatagramChannel.open()) {

            initChannel(channel);
            doSchedule(channel);

            endLatch.await();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("unable to run broadcaster", e);
        } finally {
            this.scheduler.shutdownNow();
        }
    }

    private void doSchedule(final DatagramChannel channel) {
        Objects.requireNonNull(channel);

        doSchedule(channel, new Runnable() {
                    public void run() {
                        System.out.printf("Multicasting for %s%n", Multicaster.this.id);

                        try {
                            Multicaster.this.doBroadcast(channel);
                        } catch (IOException e) {
                            e.printStackTrace(System.err);
                        }
                    }
                },
                0L,
                Constants.Schedule.PULSE_DELAY_IN_MILLISECONDS,
                TimeUnit.MILLISECONDS);
    }

    private void initChannel(final DatagramChannel channel) throws IOException {
        channel.bind(null);

        channel.setOption(StandardSocketOptions.IP_MULTICAST_IF, this.networkInterface);
    }


    private void doBroadcast(final DatagramChannel channel) throws IOException {
        buffer.clear();

        // first send the id of the server
        for (byte aByte : id.getBytes()) {
            buffer.put(aByte);
        }

        // then send the health state of the node
        for (byte aByte : ProtocolConstants.INFO_SEP.getBytes()) {
            buffer.put(aByte);
        }
        for (byte aByte : HealthNodeState.HEALTHY.name().getBytes()) {
            buffer.put(aByte);
        }


        buffer.limit(buffer.position());
        buffer.position(0);
        channel.send(buffer, multicastGroup);
    }
}
