package chriniko.kv.server;

import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.datatypes.error.UncheckedParsingException;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import chriniko.kv.protocol.Operations;
import chriniko.kv.protocol.ProtocolConstants;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class KvParser {


    public void process(final String serverName, final ByteBuffer byteBuffer, final KvStorageEngine kvStorageEngine) {

        try {
            __process(serverName, byteBuffer, kvStorageEngine);
        } catch (Exception e) {
            System.err.println("error occurred in KvParser#process, msg: " + e.getMessage());
            String errorResp = ProtocolConstants.ERROR_RESP.apply(e.getMessage());
            writeResponseMessage(byteBuffer, errorResp);
        }

    }

    private void __process(final String serverName, final ByteBuffer byteBuffer, final KvStorageEngine kvStorageEngine) {

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

            final String okayResp = ProtocolConstants.OKAY_RESP;
            System.out.println("WILL REPLY WITH: " + okayResp);
            writeResponseMessage(byteBuffer, okayResp);

        } else if (messageReceivedFromBroker.startsWith(Operations.PUT.getMsgOp())) {


            String key = messageReceivedFromBroker.split(":")[0];
            final int keySizeBeforeCleaning = key.length();

            key = key.substring(Operations.PUT.getMsgOp().length()); // note: throw the PUT
            key = key.replace("\"", ""); // note: throw "
            key = key.trim();

            String serializedValue = messageReceivedFromBroker.substring(keySizeBeforeCleaning + 1 /* Note: plus one in order to not have the : */);
            serializedValue = serializedValue.trim();

            try {
                System.out.println("SERIALIZED VALUE: " + serializedValue);
                final Value<?> deserializedValue = DatatypesAntlrParser.process(serializedValue);
                kvStorageEngine.save(key, deserializedValue);

                String okayResp = ProtocolConstants.OKAY_RESP;
                System.out.println("WILL REPLY WITH: " + okayResp);
                writeResponseMessage(byteBuffer, okayResp);

            } catch (ParsingException e) {

                System.err.println("parsing error occurred, msg: " + e.getMessage());
                String errorResp = ProtocolConstants.ERROR_RESP.apply("[parsingError]" + e.getMessage());
                writeResponseMessage(byteBuffer, errorResp);

            } catch (UncheckedParsingException e) {

                ParsingException error = e.getError();

                System.err.println("parsing error occurred, msg: " + error.getMessage());
                String errorResp = ProtocolConstants.ERROR_RESP.apply("[parsingError]" + error.getMessage());
                writeResponseMessage(byteBuffer, errorResp);

            }


        } else if (messageReceivedFromBroker.startsWith(Operations.GET.getMsgOp())) {

            final String[] s = messageReceivedFromBroker.split(" ");
            final String operation = s[0];
            final String key = s[1];

            System.out.println("get operation: " + operation + " --- key: " + key);

            final Value<?> result = kvStorageEngine.fetch(key);
            if (result != null) {

                final String serializedResult = result.asString();

                final String okayResp = ProtocolConstants.OKAY_RESP + "#" + serializedResult;
                System.out.println("WILL REPLY WITH: " + okayResp);
                writeResponseMessage(byteBuffer, okayResp);

            } else {

                final String notFoundResp = ProtocolConstants.NOT_FOUND_RESP;
                System.out.println("WILL REPLY WITH: " + notFoundResp);
                writeResponseMessage(byteBuffer, notFoundResp);

            }

        } else if (messageReceivedFromBroker.startsWith(Operations.DELETE.getMsgOp())) {

            // TODO validate string received if is valid... (RETURN ERROR)

            //todo

            String okayResp = ProtocolConstants.OKAY_RESP;
            System.out.println("WILL REPLY WITH: " + okayResp);
            writeResponseMessage(byteBuffer, okayResp);


        } else if (messageReceivedFromBroker.startsWith(Operations.QUERY.getMsgOp())) {

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
