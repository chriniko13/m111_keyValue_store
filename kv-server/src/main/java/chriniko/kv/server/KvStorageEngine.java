package chriniko.kv.server;

import java.util.concurrent.ConcurrentHashMap;

public class KvStorageEngine {

    //TODO replace with concurrent trie
    private final ConcurrentHashMap<String, String> m = new ConcurrentHashMap<>();

    public void save(String key, String value) {
        m.put(key, value);
    }

    public int totalRecords() {
        int size = m.size();
        System.out.println("total records: " + size);
        return size;
    }

    public String fetch(String key) {
        return m.get(key);
    }
}
