package chriniko.kv.broker.error.availability;

public class QuorumNotApplicableException extends KvServerAvailabilityException {

    public QuorumNotApplicableException(String msg) {
        super(msg);
    }
}
