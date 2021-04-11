package chriniko.kv.broker.error.response;

import chriniko.kv.broker.error.KvInfraException;
import lombok.Getter;

public class ErrorReceivedFromKvServerException extends KvInfraException {

    @Getter
    private final String serverResponse;

    public ErrorReceivedFromKvServerException(String msg, String serverResponse) {
        super(msg);
        this.serverResponse = serverResponse;
    }
}
