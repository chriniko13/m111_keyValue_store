package chriniko.kv.server;

import chriniko.kv.protocol.Operations;
import chriniko.kv.protocol.ProtocolConstants;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class KvParser {

    public void process(final String serverName, final ByteBuffer byteBuffer, final KvStorageEngine kvStorageEngine) {

        System.out.println("\n\n~~~~~~~KvParser(" + serverName + ")~~~~~~~~");

        System.out.println("RECEIVED BYTE BUFFER: " + byteBuffer);
        final String messageReceivedFromBroker = StandardCharsets.UTF_8.decode(byteBuffer).toString();
        System.out.println("RECEIVED MESSAGE: " + messageReceivedFromBroker);


        if (messageReceivedFromBroker.isEmpty() || messageReceivedFromBroker.isBlank()) {
            System.out.println("RECEIVED EMPTY MESSAGE FROM BROKER");
            return;
        }


        if (Operations.HEALTH_CHECK.getMsgOp().equals(messageReceivedFromBroker)) {

            String okayResp = ProtocolConstants.OKAY_RESP;
            System.out.println("WILL REPLY WITH: " + okayResp);
            writeResponseMessage(byteBuffer, okayResp);

        } else if (messageReceivedFromBroker.contains(Operations.PUT.getMsgOp())) {

            // TODO validate string received if is valid...

            String key = messageReceivedFromBroker.split(":")[0];
            final int keySizeBeforeCleaning = key.length();

            key = key.substring(Operations.PUT.getMsgOp().length()); // note: throw the PUT
            key = key.replace("\"", ""); // note: throw "
            key = key.trim();


            String value = messageReceivedFromBroker.substring(keySizeBeforeCleaning + 1 /* Note: plus one in order to not have the : */);

            value = value.trim();

            kvStorageEngine.save(key, value);

            String okayResp = ProtocolConstants.OKAY_RESP;
            System.out.println("WILL REPLY WITH: " + okayResp);
            writeResponseMessage(byteBuffer, okayResp);

        } else if (messageReceivedFromBroker.contains(Operations.GET.getMsgOp())) {

            // TODO validate string received if is valid...

            //todo

            String okayResp = ProtocolConstants.OKAY_RESP;
            System.out.println("WILL REPLY WITH: " + okayResp);
            writeResponseMessage(byteBuffer, okayResp);

        } else if (messageReceivedFromBroker.contains(Operations.DELETE.getMsgOp())) {

            // TODO validate string received if is valid...

            //todo

            String okayResp = ProtocolConstants.OKAY_RESP;
            System.out.println("WILL REPLY WITH: " + okayResp);
            writeResponseMessage(byteBuffer, okayResp);

        } else if (messageReceivedFromBroker.contains(Operations.QUERY.getMsgOp())) {

            // TODO validate string received if is valid...

            //todo

            String okayResp = ProtocolConstants.OKAY_RESP;
            System.out.println("WILL REPLY WITH: " + okayResp);
            writeResponseMessage(byteBuffer, okayResp);

        } else {

            String unknownCommandResp = ProtocolConstants.UNKNOWN_COMMAND_RESP;
            System.out.println("WILL REPLY WITH: " + unknownCommandResp);
            writeResponseMessage(byteBuffer, unknownCommandResp);
        }

    }

    private void writeResponseMessage(ByteBuffer byteBuffer, String responseMessage) {
        byte[] bytes = responseMessage.getBytes(StandardCharsets.UTF_8);

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
