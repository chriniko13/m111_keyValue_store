package chriniko.kv.server;

import chriniko.kv.datatypes.Value;

import java.util.concurrent.ConcurrentHashMap;

public class KvStorageEngine {

    //TODO replace with concurrent trie
    private final ConcurrentHashMap<String, Value<?>> m = new ConcurrentHashMap<>();


    public void save(String key, Value<?> v) {
        m.put(key, v);
    }

    public int totalRecords() {
        int size = m.size();
        System.out.println("total records: " + size);
        return size;
    }

    public Value<?> fetch(String key) {
        return m.get(key);
    }
}
