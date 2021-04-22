package chriniko.kv.server.infra;

import chriniko.kv.protocol.ProtocolConstants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KvServer {

    // Every socket has its own ByteBuffer to operate independently.
    private static final ConcurrentHashMap<SocketChannel, ByteBuffer> sockets = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<SocketChannel, String /*address*/> clientAddressesBySockets = new ConcurrentHashMap<>();

    private final String serverName;
    private final KvRequestParser kvRequestParser;
    private final KvStorageEngine kvStorageEngine;
    private final KvServerConfig kvServerConfig;

    private ServerSocketChannel serverSocket;


    private KvServer(String serverName, KvRequestParser kvRequestParser, KvStorageEngine kvStorageEngine, KvServerConfig kvServerConfig) {
        this.serverName = serverName;
        this.kvRequestParser = kvRequestParser;
        this.kvStorageEngine = kvStorageEngine;
        this.kvServerConfig = kvServerConfig;
    }

    public static KvServer create(String serverName) {

        KvServerConfig serverConfig = KvServerConfig.createDefault();

        return new KvServer(serverName,
                new KvRequestParser(serverConfig),
                new KvStorageEngine(),
                serverConfig
        );
    }
    public static KvServer create(String serverName, KvServerConfig kvServerConfig) {
        return new KvServer(serverName,
                new KvRequestParser(kvServerConfig),
                new KvStorageEngine(),
                kvServerConfig
        );
    }

    public KvStorageEngine getStorageEngine() {
        return kvStorageEngine;
    }

    public void stop() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ignored) {
            }
        }
    }

    public void run(String hostname, int port, Runnable callbackToExecuteWhenServerIsUp) throws IOException {

        serverSocket = ServerSocketChannel.open();

        // Binding this server on the port
        InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, port);
        serverSocket.bind(inetSocketAddress);

        serverSocket.configureBlocking(false); // Make Server nonBlocking

        final Selector selector = Selector.open();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT); // Interested only in Accept connection

        System.out.println(">>>" + serverName + "### kvServer is up and running at: " + inetSocketAddress);
        callbackToExecuteWhenServerIsUp.run();


        while (true) {
            selector.select(); // BLocks until something happens
            final Set<SelectionKey> selectionKeys = selector.selectedKeys();

            for (Iterator<SelectionKey> it = selectionKeys.iterator(); it.hasNext(); ) {
                final SelectionKey key = it.next();
                it.remove(); // Remove to not get the same key again and again

                try {

                    if (key.isValid()) {
                        if (key.isAcceptable()) {
                            accept(key); // Got something on Accept event
                        } else if (key.isWritable()) {
                            write(key); // Got something on Write event
                        } else if (key.isReadable()) {
                            read(key); // Got something on Read event
                        }
                    } else {
                        System.err.println("key is not valid: " + key);
                    }

                } catch (final ClosedChannelException e) {

                    // Note: in case a socket is closed (from client) we catch in order to not terminate the kv-server and
                    //       we log only the error so to know what is going on...

                    SelectableChannel channel = key.channel();
                    SocketChannel sc = (SocketChannel) channel;
                    String address = clientAddressesBySockets.get(sc);

                    System.err.println("closed channel exception occurred: " + e + " from client: " + address);

                } catch (final IOException e) {

                    System.err.println("unhandled io exception occurred: " + e.getMessage());
                    e.printStackTrace(System.err);

                    closeChannel(key);


                    // Note: will terminate so that to make bold the error and fix it during development time.
                    System.exit(800);

                } catch (final Exception unknown) {

                    System.out.println("UNKNOWN EXCEPTION OCCURRED: " + unknown.getMessage());
                    unknown.printStackTrace(System.err);

                    closeChannel(key);


                    // Note: will terminate so that to make bold the error and fix it during development time.
                    System.exit(801);

                }
            } // for.

            // Remove sockets which are no longer open
            sockets.keySet().removeIf((socketChannel) -> !socketChannel.isOpen());
            clientAddressesBySockets.keySet().removeIf((socketChannel -> !socketChannel.isOpen()));

        } // while.
    }


    // Once the Accept event is received, allocate the ByteBuffer and star reading from it(by getting interested in READ Operation)
    private void accept(final SelectionKey key) throws IOException {
        ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socket = socketChannel.accept(); // Non Blocking BUT WILL ALWAYS HAVE SOME DATA(NEVER NULL)

        socket.configureBlocking(false); // Required, socket should also be NonBlocking

        socket.register(key.selector(), SelectionKey.OP_READ); // Interested only in Reading from the socket.

        // Every socket will have its own byte buffer
        sockets.put(socket, ByteBuffer.allocateDirect(ProtocolConstants.BYTES_TO_ALLOCATE_PER_BUFFER));
        clientAddressesBySockets.put(socket, "local->" + socket.getLocalAddress().toString() + " # remote->" + socket.getRemoteAddress().toString());
        System.out.println("### ACCEPTED CONNECTION FROM: " + socket);
    }

    // Once the Read event is received, perform the operation on the input, and then write back once the
    // Write Operation is received.(So getting interested in Write Operation)
    private void read(final SelectionKey key) throws IOException {
        final SocketChannel socket = (SocketChannel) key.channel();
        final ByteBuffer byteBuffer = sockets.get(socket);

        byteBuffer.clear();

        System.out.println("### READ CONTENTS FROM CONNECTION: " + socket);
        int data = socket.read(byteBuffer);

        if (data == -1) {
            closeSocket(socket);
            sockets.remove(socket);
        }

        kvRequestParser.process(serverName, byteBuffer, kvStorageEngine);

        socket.configureBlocking(false); // Required, socket should also be NonBlocking

        key.interestOps(SelectionKey.OP_WRITE);  // Now listen for write, as the operation is done and we need to write it down.
    }

    // Once the Write event is received, write the associated ByteBuffer with this socket.
    private void write(final SelectionKey key) throws IOException {
        final SocketChannel socket = (SocketChannel) key.channel();
        final ByteBuffer byteBuffer = sockets.get(socket);

        socket.write(byteBuffer); // Wont always write everything
        while (!byteBuffer.hasRemaining()) {
            byteBuffer.compact();
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void closeChannel(SelectionKey key) {
        SelectableChannel channel = key.channel();

        if (channel instanceof ServerSocketChannel) {

            System.out.println("will close serverSocketChannel: " + channel);

        } else if (channel instanceof SocketChannel) {

            System.out.println("will close socketChannel: " + channel);
            sockets.remove(channel);
            clientAddressesBySockets.remove(channel);

        } else {
            System.out.println("will close unknown selectableChannel: " + channel);

        }

        try {
            channel.close();
        } catch (IOException e) {
            System.err.println("error occurred during closeChannel socket: " + e);
        }

    }

    private void closeSocket(final SocketChannel socket) {
        try {
            socket.close();
        } catch (IOException ignored) {
            System.err.println("error occurred during close socket: " + ignored);
        }
    }
}
