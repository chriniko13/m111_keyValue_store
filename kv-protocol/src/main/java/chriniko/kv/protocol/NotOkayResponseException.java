package chriniko.kv.protocol;

/**
 * This exception represents a response which is NOT equal to {@link ProtocolConstants#OKAY_RESP}
 */
public class NotOkayResponseException extends Exception {

    public NotOkayResponseException(String msg) {
        super(msg);
    }
}
