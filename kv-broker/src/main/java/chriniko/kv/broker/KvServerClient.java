package chriniko.kv.broker;

import chriniko.kv.protocol.ProtocolConstants;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

@EqualsAndHashCode(of = {"socketChannel"})
public class KvServerClient {

    private final String host;
    private final int port;

    private final SocketChannel socketChannel;

    private ByteBuffer buffer;

    public static KvServerClient start(String host, int port) throws IOException {
        return new KvServerClient(host, port);
    }

    public KvServerClient makeCopy() throws IOException {
        return new KvServerClient(this.host, this.port);
    }

    public void stop() throws IOException {
        socketChannel.close();
        buffer = null;
    }

    private KvServerClient(String host, int port) throws IOException {
        this.host = host;
        this.port = port;

        socketChannel = SocketChannel.open(new InetSocketAddress(host, port));
        socketChannel.configureBlocking(true);
        buffer = ByteBuffer.allocateDirect(ProtocolConstants.BYTES_TO_ALLOCATE_PER_BUFFER);
    }

    public String sendMessage(String msg) throws IOException {

        System.out.println(this + "::BOOM::" + Thread.currentThread().getName() + " --- " + this.hashCode());

        // Note: prepare message that we want to send
        byte[] bytes = msg.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            buffer.put(i, bytes[i]);
        }
        buffer.position(0);
        buffer.limit(bytes.length);

        System.out.println("~~~~~~~~~~~~~~~~~~~~");

        // Note: send message
        System.out.println("WILL SEND BYTE BUFFER TO SERVER: " + buffer);
        socketChannel.write(buffer);

        // Note: get response
        System.out.println("NOW WILL READ RESPONSE FROM SERVER...");
        buffer.clear();
        socketChannel.read(buffer);
        System.out.println("RESPONSE FROM SERVER JUST READ...");


        // Note: read response correctly
        System.out.println("BYTE BUFFER AFTER READING RESPONSE FROM SERVER: " + buffer);
        buffer.limit(buffer.position());
        buffer.position(0);
        System.out.println("(ADAPT)BYTE BUFFER AFTER READING RESPONSE FROM SERVER: " + buffer);

        final byte[] slice = new byte[buffer.limit()];
        buffer.get(slice, 0, buffer.limit());
        final String sliceAsString = new String(slice);
        System.out.println("SLICE: " + Arrays.toString(slice) + ", AS STR: " + sliceAsString);

        System.out.println("RESPONSE RECEIVED FROM SERVER: " + sliceAsString);

        buffer.clear();
        return sliceAsString;

    }

}
