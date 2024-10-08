package chriniko.kv.trie;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;


@Getter
@Setter
public class TrieNode<T> {

    private final HashMap<Character, TrieNode<T>> children;

    private boolean isCompleteWord;
    private String prefix;

    private T data;

    // when override happens for data, we put here the old data.
    private final LinkedList<T> oldData;

    public TrieNode() {
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
}
