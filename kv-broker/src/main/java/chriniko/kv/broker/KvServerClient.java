package chriniko.kv.broker;

import chriniko.kv.protocol.ProtocolConstants;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class KvServerClient {

    private static SocketChannel socketChannel;
    private static ByteBuffer buffer;
    private static KvServerClient instance;

    public static KvServerClient start(String host, int port) throws IOException {
        if (instance == null) {
            instance = new KvServerClient(host, port);
        }

        return instance;
    }

    public void stop() throws IOException {
        socketChannel.close();
        buffer = null;
    }

    private KvServerClient(String host, int port) throws IOException {
        socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
        buffer = ByteBuffer.allocate(ProtocolConstants.BYTES_TO_ALLOCATE_PER_BUFFER);
    }

    public String sendMessage(String msg) throws IOException {
        buffer = ByteBuffer.wrap(msg.getBytes()); // TODO optimize....

        System.out.println("~~~~~~~~~~~~~~~~~~~~");

        System.out.println("WILL SEND BYTE BUFFER TO SERVER: " + buffer);
        socketChannel.write(buffer);

        buffer.clear();
        socketChannel.read(buffer);
        System.out.println("BYTE BUFFER AFTER READING RESPONSE FROM SERVER: " + buffer);
        buffer.limit(buffer.position());
        buffer.position(0);
        System.out.println("(ADAPT)BYTE BUFFER AFTER READING RESPONSE FROM SERVER: " + buffer);


        byte[] slice = new byte[buffer.limit()];
        buffer.get(slice, 0, buffer.limit());
        String sliceAsString = new String(slice);
        System.out.println("SLICE: " + Arrays.toString(slice) + ", AS STR: " + sliceAsString);

        String response = sliceAsString;
        System.out.println("RESPONSE RECEIVED FROM SERVER: " + response);


        buffer.clear();
        return response;
    }

}
