package chriniko.kv.broker.error.availability;

import chriniko.kv.broker.error.KvInfraException;

public class KvServerAvailabilityException extends KvInfraException {
    public KvServerAvailabilityException(String msg) {
        super(msg);
    }
}
