package chriniko.kv.broker.error.response;

import chriniko.kv.broker.error.KvInfraException;
import chriniko.kv.protocol.ErrorTypeConstants;
import lombok.Getter;

public class ErrorReceivedFromKvServerException extends KvInfraException {

    @Getter
    private final String serverResponse;

    public ErrorReceivedFromKvServerException(String msg, String serverResponse) {
        super(msg);
        this.serverResponse = serverResponse;
    }

    public boolean isParsingError() {
        return serverResponse.contains(ErrorTypeConstants.PARSING_ERROR.name());
    }

    public boolean isIndexError() {
        return serverResponse.contains(ErrorTypeConstants.INDEX_ERROR.name());
    }
}
