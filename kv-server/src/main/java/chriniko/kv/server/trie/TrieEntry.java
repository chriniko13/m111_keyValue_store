package chriniko.kv.server.trie;

import java.time.Instant;

public interface TrieEntry {

    String key();

    void setKey(String k);

    Instant createTime();

    Instant updateTime();

    void triggerUpdateTime();
}
