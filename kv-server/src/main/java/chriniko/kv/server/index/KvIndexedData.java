package chriniko.kv.server.index;

import chriniko.kv.datatypes.Value;
import chriniko.kv.trie.Trie;
import chriniko.kv.trie.TrieEntry;

import java.time.Instant;
import java.util.LinkedHashMap;

public class KvIndexedData implements TrieEntry<Value<?>> {

    private final String key;
    private final Value<?> value;


    private final Instant createTime;
    private Instant updateTime;


    // ---

    public KvIndexedData(String key, Value<?> value) {
        this.key = key;
        this.value = value;

        this.createTime = Instant.now();
    }


    // ---

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


    // ---

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


    // ---
    @Override
    public void indexContents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public LinkedHashMap<String, Value<?>> getIndexedContentsByKeyPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Trie<?> getIndexedContentsByKeyPathTrie() {
        throw new UnsupportedOperationException();
    }
}
