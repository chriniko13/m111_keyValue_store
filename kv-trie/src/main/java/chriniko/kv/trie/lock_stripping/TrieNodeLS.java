package chriniko.kv.trie.lock_stripping;


import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


@Getter
@Setter
public class TrieNodeLS<T> {

    private final HashMap<Character,TrieNodeLS<T>> children;

    private boolean isCompleteWord;
    private String prefix;

    private T data;

    // when override happens for data, we put here the old data.
    private final LinkedList<T> oldData;

    public TrieNodeLS() {
        children = new HashMap<>();
        oldData = new LinkedList<>();
    }

    public void storeDataAsOld() {
        oldData.add(data);
    }

    public List<T> getOldData() {
        return Collections.unmodifiableList(oldData);
    }

    public void clear() {

        setCompleteWord(false);
        setPrefix("");

        data = null;
        children.clear();
    }


    // --- lock infra ---

    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);


    public Lock readLock() {
        return readWriteLock.readLock();
    }

    public Lock writeLock() {
        return readWriteLock.writeLock();
    }
}
