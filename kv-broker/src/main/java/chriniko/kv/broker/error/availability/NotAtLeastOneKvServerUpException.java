package chriniko.kv.broker.error.availability;

public class NotAtLeastOneKvServerUpException extends KvServerAvailabilityException {

    public NotAtLeastOneKvServerUpException(String msg) {
        super(msg);
    }
}
