package chriniko.kv.server.trie;

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
}
