package chriniko.kv.trie;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;


@Getter
@Setter
public class TrieNode<T> {

    private final HashMap<Character, TrieNode<T>> children;

    private boolean isCompleteWord;
    private String prefix;

    private T data;


    public TrieNode() {
        children = new HashMap<>();
    }

    public void clear() {

        setCompleteWord(false);
        setPrefix("");

        data = null;
        children.clear();
    }
}
