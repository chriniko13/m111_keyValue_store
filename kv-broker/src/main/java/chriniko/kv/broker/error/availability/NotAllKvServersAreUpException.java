package chriniko.kv.broker.error.availability;

public class NotAllKvServersAreUpException extends KvServerAvailabilityException {

    public NotAllKvServersAreUpException(String msg) {
        super(msg);
    }
}
