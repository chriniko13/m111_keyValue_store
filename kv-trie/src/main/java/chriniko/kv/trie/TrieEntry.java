package chriniko.kv.trie;

import lombok.Value;

import java.time.Instant;
import java.util.LinkedHashMap;

public interface TrieEntry<T> {

    // ---


    String key();

    void setKey(String k);

    T value();


    // ---

    Instant createTime();

    Instant updateTime();

    void triggerUpdateTime();


    // ---

    void indexContents() throws Exception;

    LinkedHashMap<String, T> getIndexedContents();
}
