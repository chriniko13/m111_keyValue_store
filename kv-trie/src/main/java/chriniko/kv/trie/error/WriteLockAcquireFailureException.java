package chriniko.kv.trie.error;

public class WriteLockAcquireFailureException extends Exception {
    public WriteLockAcquireFailureException(String msg) {
        super(msg);
    }
}
