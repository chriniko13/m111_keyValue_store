package chriniko.kv.trie;

import java.time.Instant;

public interface TrieEntry<T> {

    // ---


    String key();

    void setKey(String k);

    T value();


    // ---

    Instant createTime();

    Instant updateTime();

    void triggerUpdateTime();
}
