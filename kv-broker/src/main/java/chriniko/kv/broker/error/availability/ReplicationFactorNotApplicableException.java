package chriniko.kv.broker.error.availability;

public class ReplicationFactorNotApplicableException extends  KvServerAvailabilityException {

    public ReplicationFactorNotApplicableException(String msg) {
        super(msg);
    }
}
