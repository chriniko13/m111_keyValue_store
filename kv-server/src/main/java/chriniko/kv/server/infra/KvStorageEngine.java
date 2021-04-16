package chriniko.kv.server.infra;

import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.server.error.KvServerIndexErrorException;
import chriniko.kv.server.index.KvIndexedData;
import chriniko.kv.trie.Trie;
//import chriniko.kv.trie.TrieNode;
import chriniko.kv.trie.error.ReadLockAcquireFailureException;
import chriniko.kv.trie.error.StaleDataOperationException;
import chriniko.kv.trie.error.WriteLockAcquireFailureException;
import chriniko.kv.trie.infra.TrieStatistics;
import chriniko.kv.trie.lock_stripping.TrieLS;
import chriniko.kv.trie.lock_stripping.TrieNodeLS;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ForkJoinPool;

public class KvStorageEngine {

    //private final Trie<KvRecord> memoDb = new Trie<>();
    private final TrieLS<KvRecord> memoDb = new TrieLS<>(400, 600);


    private final ForkJoinPool cpuBoundWorkers;

    public KvStorageEngine() {
        cpuBoundWorkers = new ForkJoinPool();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("will shutdown cpuBoundWorkers forkJoinPool...");
            cpuBoundWorkers.shutdown();
        }));
    }

    // Note: save operation behaves like upsert (insert on new, upsert/override on existing)
    public void save(String key, Value<?> v, boolean asyncIndexing) throws KvServerIndexErrorException, ReadLockAcquireFailureException, WriteLockAcquireFailureException, StaleDataOperationException {

        final TrieNodeLS<KvRecord> justInsertedNode = memoDb.insert(key, new KvRecord(key, v));

        if (!asyncIndexing) {
            indexRecord(justInsertedNode.getData());
        } else {
            CompletableFuture.runAsync(() -> {
                try {
                    indexRecord(justInsertedNode.getData());
                } catch (KvServerIndexErrorException e) {
                    throw new CompletionException(e);
                }
            }, cpuBoundWorkers);
        }
    }

    private void indexRecord(KvRecord kvRecord) throws KvServerIndexErrorException {
        try {
            kvRecord.indexContents();
        } catch (ParsingException e) {
            throw new KvServerIndexErrorException("indexContents failed - seems like a bug error", e);
        }
    }

    public int totalRecords() throws ReadLockAcquireFailureException {
        TrieStatistics<KvRecord> trieStatistics = memoDb.gatherStatisticsWithRecursion();
        int size = trieStatistics.getCountOfCompleteWords();
        System.out.println("total records: " + size);
        return size;
    }

    // Note: maybe it is better to have 2 versions, one with lock, one with no lock.
    public List<KvRecord> allRecords() throws ReadLockAcquireFailureException {
        TrieStatistics<KvRecord> trieStatistics = memoDb.gatherStatisticsWithRecursion();
        return trieStatistics.getValues();
    }

    public Value<?> fetch(String key) throws ReadLockAcquireFailureException {
        return memoDb.find(key)
                .map(KvRecord::value)
                .orElse(null);
    }

    public Value<?> remove(String key) throws ReadLockAcquireFailureException, WriteLockAcquireFailureException, StaleDataOperationException {
        return memoDb.delete(key)
                .map(KvRecord::value)
                .orElse(null);
    }

    public Value<?> query(String rootKey, String queryKey, boolean useTrie) throws ReadLockAcquireFailureException {

        if (queryKey.isEmpty() || !queryKey.contains("~>")) {
            return fetch(rootKey);
        } else {

            // first find record with the top key (O(n) n is the length of the top key)
            final KvRecord record = memoDb.find(rootKey).orElse(null);
            if (record == null) {
                return null;
            }


            // now search for indexed data based on the selected index data structure
            if (useTrie) {

                // indexed search with trie is O(n) where n is the length of the keypath/keys
                final Trie<KvIndexedData> indexedDataTrie = record.getIndexedContentsByKeyPathTrie();

                final KvIndexedData indexedData = indexedDataTrie.find(queryKey).orElse(null);
                if (indexedData == null) {
                    System.out.println("indexed data (trie) retrieved: null");
                    return null;
                } else {
                    Value<?> indexedRecord = indexedData.value();
                    System.out.println("indexed data (trie) retrieved: " + indexedRecord);
                    return indexedRecord;
                }

            } else {

                // indexed search with map is O(1)
                final LinkedHashMap<String, Value<?>> indexedData = record.getIndexedContentsByKeyPath();

                final Value<?> value = indexedData.get(queryKey);
                System.out.println("indexed data (map) retrieved: " + value);
                return value;

            }

        }
    }
}
