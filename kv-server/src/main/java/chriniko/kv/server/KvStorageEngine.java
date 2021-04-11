package chriniko.kv.server;

import chriniko.kv.datatypes.Value;
import chriniko.kv.trie.Trie;

public class KvStorageEngine {

    private final Trie<KvRecord> memoDb = new Trie<>();

    public void save(String key, Value<?> v) {
        memoDb.insert(key, new KvRecord(key, v));
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
}
