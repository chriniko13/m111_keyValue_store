package chriniko.kv.server;

import chriniko.kv.datatypes.Value;
import chriniko.kv.trie.Trie;
import chriniko.kv.trie.TrieNode;

public class KvStorageEngine {

    private final Trie<KvRecord> memoDb = new Trie<>();

    // Note: save operation behaves like upsert (insert on new, upsert/override on existing)
    public void save(String key, Value<?> v) {
        TrieNode<KvRecord> justInsertedNode = memoDb.insert(key, new KvRecord(key, v));
        justInsertedNode.getData().indexContents();
    }

    public int totalRecords() {
        Trie<KvRecord>.TrieStatistics trieStatistics = memoDb.gatherStatisticsWithRecursion();
        int size = trieStatistics.getCountOfCompleteWords();
        System.out.println("total records: " + size);
        return size;
    }

    public Value<?> fetch(String key) {
        return memoDb.find(key)
                .map(KvRecord::value)
                .orElse(null);
    }

    public Value<?> remove(String key) {
        return memoDb.delete(key)
                .map(KvRecord::value)
                .orElse(null);
    }
}
