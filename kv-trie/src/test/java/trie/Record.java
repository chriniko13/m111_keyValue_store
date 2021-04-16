package trie;

import chriniko.kv.trie.Trie;
import chriniko.kv.trie.TrieEntry;
import lombok.ToString;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.UUID;

// --- infra ---
@ToString
class Record implements TrieEntry<String> {
    private String key;
    private final String value = UUID.randomUUID().toString();

    private final Instant createTime = Instant.now();
    private Instant updateTime;

    @Override
    public String key() {
        return key;
    }

    @Override
    public void setKey(String k) {
        key = k;
    }

    @Override
    public String value() {
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

    @Override
    public void indexContents() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public LinkedHashMap<String, String> getIndexedContentsByKeyPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Trie<?> getIndexedContentsByKeyPathTrie() {
        throw new UnsupportedOperationException();
    }

    public String getValue() {
        return value;
    }
}
