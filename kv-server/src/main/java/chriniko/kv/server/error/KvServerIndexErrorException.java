package chriniko.kv.server.error;

public class KvServerIndexErrorException extends KvServerInfraException {
    public KvServerIndexErrorException(String msg) {
        super(msg);
    }

    public KvServerIndexErrorException(String msg, Throwable error) {
        super(msg, error);
    }
}
