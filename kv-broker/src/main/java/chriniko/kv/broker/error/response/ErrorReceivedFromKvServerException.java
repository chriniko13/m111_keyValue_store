package chriniko.kv.broker.error.response;

import chriniko.kv.broker.error.KvInfraException;

public class ErrorReceivedFromKvServerException extends KvInfraException {

    public ErrorReceivedFromKvServerException(String msg) {
        super(msg);
    }
}
