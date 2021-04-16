package chriniko.kv.server.infra;

import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.server.index.KvIndexedData;
import chriniko.kv.server.index.KvRecordIndexer;
import chriniko.kv.trie.Trie;
import chriniko.kv.trie.infra.TrieEntry;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

public class KvRecord implements TrieEntry<Value<?>> {

    private final String key;
    private final Value<?> value;

    private final Instant createTime;
    private Instant updateTime;

    // --- indexed record data ---
    private LinkedHashMap<String /*path, eg: address~>street*/, Value<?>> indexedContentsByKeyPath;
    private Trie<KvIndexedData> indexedContentsByKeyPathTrie;


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

    public void indexContents() throws ParsingException {
        String serialized = value.asString();

        // index contents by key path in map (time complexity: O(1))
        indexedContentsByKeyPath = KvRecordIndexer.process(serialized);


        // index contents by key path in trie (time complexity: O(n) where n is the length of the key path)
        indexedContentsByKeyPathTrie = new Trie<>();
        for (Map.Entry<String, Value<?>> entry : indexedContentsByKeyPath.entrySet()) {

            String k = entry.getKey();
            Value<?> v = entry.getValue();
            KvIndexedData indexedData = new KvIndexedData(k, v);

            indexedContentsByKeyPathTrie.insert(k, indexedData);
        }
    }

    @Override
    public LinkedHashMap<String, Value<?>> getIndexedContentsByKeyPath() {
        return indexedContentsByKeyPath;
    }

    @Override
    public Trie<KvIndexedData> getIndexedContentsByKeyPathTrie() {
        return indexedContentsByKeyPathTrie;
    }
}
