package chriniko.kv.server;

import chriniko.kv.protocol.ProtocolConstants;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class KvParser {


    public void process(ByteBuffer byteBuffer) {


        System.out.println("~~~~~~~~~~~~~~~");

        System.out.println("RECEIVED BYTE BUFFER: " + byteBuffer);
        String s = StandardCharsets.UTF_8.decode(byteBuffer).toString();
        System.out.println("RECEIVED MESSAGE: " + s);


        if (s.isEmpty() || s.isBlank()) {
            System.out.println("RECEIVED EMPTY MESSAGE FROM BROKER");
            return;
        }


        //TODO write parsing logic (based on operation, etc.)....




        String okayResp = ProtocolConstants.OKAY_RESP;
        System.out.println("WILL REPLY WITH: " + okayResp);


        byte[] bytes = okayResp.getBytes(StandardCharsets.UTF_8);


        int idx = 0;
        for (byte b : bytes) {
            byteBuffer.put(idx++, b);
        }
        byteBuffer.rewind();
        byteBuffer.limit(bytes.length);

        System.out.println("REPLY BYTEBUFFER: " + byteBuffer);

    }

    /*
      for (int x = 0; x < byteBuffer.limit(); x++) { // read every byte in it.
            byteBuffer.put(x, (byte) invertCase(byteBuffer.get(x)));
        }
     */
}
