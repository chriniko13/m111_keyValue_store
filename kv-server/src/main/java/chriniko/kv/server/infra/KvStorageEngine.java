package chriniko.kv.server.infra;

import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.server.error.KvServerIndexErrorException;
import chriniko.kv.trie.Trie;
import chriniko.kv.trie.TrieNode;

public class KvStorageEngine {

    private final Trie<KvRecord> memoDb = new Trie<>();

    // Note: save operation behaves like upsert (insert on new, upsert/override on existing)
    public void save(String key, Value<?> v) throws KvServerIndexErrorException {
        TrieNode<KvRecord> justInsertedNode = memoDb.insert(key, new KvRecord(key, v));

        //todo this can be made async...
        try {
            justInsertedNode.getData().indexContents();
        } catch (ParsingException e) {
            throw new KvServerIndexErrorException("indexContents failed - seems like a bug error", e);
        }
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
