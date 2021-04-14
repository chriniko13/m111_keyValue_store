package chriniko.kv.server.error;

public class KvServerInfraException extends Exception {

    public KvServerInfraException(String msg) {
        super(msg);
    }

    public KvServerInfraException(String msg, Throwable error) {
        super(msg, error);
    }
}
