package chriniko.kv.trie.error;

public class ReadLockAcquireFailureException extends Exception {

    public ReadLockAcquireFailureException(String msg) {
        super(msg);
    }
}
