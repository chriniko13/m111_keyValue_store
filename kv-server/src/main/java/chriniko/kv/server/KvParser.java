package chriniko.kv.server;

import chriniko.kv.protocol.Operations;
import chriniko.kv.protocol.ProtocolConstants;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class KvParser {

    public void process(final String serverName, final ByteBuffer byteBuffer, final KvStorageEngine kvStorageEngine) {

        // Note: flip the byte buffer so that we can read from the start correctly.
        byteBuffer.flip();

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

            // TODO validate string received if is valid... (RETURN ERROR)

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

            // TODO validate string received if is valid... (RETURN ERROR)

            String[] s = messageReceivedFromBroker.split(" ");
            String operation = s[0];
            String key = s[1];

            System.out.println("get operation: " + operation + " --- key: " + key);

            String result = kvStorageEngine.fetch(key);
            if (result != null) {

                String okayResp = ProtocolConstants.OKAY_RESP + "#" + result;
                System.out.println("WILL REPLY WITH: " + okayResp);
                writeResponseMessage(byteBuffer, okayResp);

            } else {

                String notFoundResp = ProtocolConstants.NOT_FOUND_RESP;
                System.out.println("WILL REPLY WITH: " + notFoundResp);
                writeResponseMessage(byteBuffer, notFoundResp);

            }



        } else if (messageReceivedFromBroker.contains(Operations.DELETE.getMsgOp())) {

            // TODO validate string received if is valid... (RETURN ERROR)

            //todo

            String okayResp = ProtocolConstants.OKAY_RESP;
            System.out.println("WILL REPLY WITH: " + okayResp);
            writeResponseMessage(byteBuffer, okayResp);

        } else if (messageReceivedFromBroker.contains(Operations.QUERY.getMsgOp())) {

            // TODO validate string received if is valid... (RETURN ERROR)

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

        System.out.println("KvParser#writeResponseMessage: " + new String(bytes) + " byteBuffer: " + byteBuffer);

        // Note: now that we have read, time to clear the buffer so that we can write.
        byteBuffer.clear();

        int idx = 0;
        for (byte b : bytes) {
            byteBuffer.put(idx++, b);
        }

        byteBuffer.rewind();
        byteBuffer.limit(bytes.length);

        System.out.println("REPLY BYTEBUFFER: " + byteBuffer);

        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~\n\n");
    }

}
