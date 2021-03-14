package chriniko.kv.server;

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

    private final KvParser kvParser;

    public KvServer(KvParser kvParser) {
        this.kvParser = kvParser;
    }


    public void run(String hostname, int port) throws IOException {

        final ServerSocketChannel serverSocket = ServerSocketChannel.open(); // Creating the server on port 8080

        // Binding this server on the port
        InetSocketAddress inetSocketAddress = new InetSocketAddress(hostname, port);
        serverSocket.bind(inetSocketAddress);

        serverSocket.configureBlocking(false); // Make Server nonBlocking

        final Selector selector = Selector.open();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT); // Interested only in Accept connection

        System.out.println("### kvServer is up and running at: " + inetSocketAddress);

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
                            write(key); //// Got something on Write event
                        } else if (key.isReadable()) {
                            read(key); // Got something on Read event
                        }
                    } else {
                        System.err.println("key is not valid: " + key);
                    }

                } catch (final ClosedChannelException e) {
                    // Note: in case a socket is closed (from client) we catch in order to not terminate the kv-server and
                    //       we log only the error so to know what is going on...
                    System.err.println("closed channel exception occurred: " + e); //TODO print also the IP of client...
                }
            }

            // Remove sockets which are no longer open
            sockets.keySet().removeIf((socketChannel) -> !socketChannel.isOpen());
        }
    }



    // Once the Accept event is received, allocate the ByteBuffer and star reading from it(by getting interested in READ Operation)
    private void accept(final SelectionKey key) throws IOException {
        ServerSocketChannel socketChannel = (ServerSocketChannel) key.channel();
        SocketChannel socket = socketChannel.accept(); // Non Blocking BUT WILL ALWAYS HAVE SOME DATA(NEVER NULL)

        socket.configureBlocking(false); // Required, socket should also be NonBlocking

        socket.register(key.selector(), SelectionKey.OP_READ); // Interested only in Reading from the socket.

        // Every socket will have its own byte buffer
        sockets.put(socket, ByteBuffer.allocateDirect(ProtocolConstants.BYTES_TO_ALLOCATE_PER_BUFFER));
    }

    // Once the Read event is received, perform the operation on the input, and then write back once the
    // Write Operation is received.(So getting interested in Write Operation)
    private void read(final SelectionKey key) throws IOException {
        final SocketChannel socket = (SocketChannel) key.channel();
        final ByteBuffer byteBuffer = sockets.get(socket);
        int data = socket.read(byteBuffer);

        if (data == -1) {
            closeSocket(socket);
            sockets.remove(socket);
        }

        byteBuffer.flip();
        kvParser.process(byteBuffer);

        socket.configureBlocking(false); // Required, socket should also be NonBlocking

        key.interestOps(SelectionKey.OP_WRITE);
        // Now listen for write, as the operation is done and we need to write it down.
    }

    // Once the Write event is received, write the associated ByteBuffer with this socket.
    private void write(final SelectionKey key) throws IOException {
        final SocketChannel socket = (SocketChannel) key.channel();
        final ByteBuffer byteBuffer = sockets.get(socket); // Its already case inverted

        System.out.println("WILL WRITE NOW TO THE CLIENT BYTE BUFFER: " + byteBuffer);

        socket.write(byteBuffer); // Wont always write everything
        while (!byteBuffer.hasRemaining()) {
            byteBuffer.compact();
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void closeSocket(final SocketChannel socket) {
        try {
            socket.close();
        } catch (IOException ignore) {
            System.err.println("error occurred during close socket: " + ignore);
        }
    }



    // TODO left as an example - remove when kv parser implemented...
    private static void invertCase(final ByteBuffer byteBuffer) {
        for (int x = 0; x < byteBuffer.limit(); x++) { // read every byte in it.
            byteBuffer.put(x, (byte) invertCase(byteBuffer.get(x)));
        }
    }

    // TODO left as an example - remove when kv parser implemented...
    private static int invertCase(final int data) {
        return Character.isLetter(data) ?

                Character.isUpperCase(data)
                        ? Character.toLowerCase(data)
                        : Character.toUpperCase(data) :

                data;
    }

}
