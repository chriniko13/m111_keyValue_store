package chriniko.kv.server;

import chriniko.kv.datatypes.Value;
import chriniko.kv.trie.TrieEntry;

import java.time.Instant;

public class KvRecord implements TrieEntry<Value<?>> {

    private final String key;
    private final Value<?> value;

    private final Instant createTime;
    private Instant updateTime;

    public KvRecord(String key, Value<?> value) {
        this.key = key;
        this.value = value;

        this.createTime = Instant.now();
    }

    @Override
    public String key() {
        return key;
    }

    @Override
    public void setKey(String k) {
        if (!key.equals(k)) {
            throw new IllegalStateException("KvRecord (k != key)");
        }
    }

    @Override
    public Value<?> value() {
        return value;
    }

    @Override
    public Instant createTime() {
        return createTime;
    }

    @Override
    public Instant updateTime() {
        return updateTime;
    }

    @Override
    public void triggerUpdateTime() {
        updateTime = Instant.now();
    }
}
