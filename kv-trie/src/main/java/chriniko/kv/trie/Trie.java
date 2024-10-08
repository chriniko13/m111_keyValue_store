package chriniko.kv.trie;

import chriniko.kv.trie.infra.TrieEntry;
import chriniko.kv.trie.infra.TrieStatistics;

import java.util.*;

public class Trie<T extends TrieEntry<?>> {

    private static final boolean EAGER_DELETION = true;

    private final TrieNode<T> root;

    public Trie() {
        root = new TrieNode<>();
        root.setPrefix("");
        root.setCompleteWord(false);
    }

    public void clear() {
        root.clear();
        System.gc(); // give hint for gc process
    }


    // Note: The time complexity is O(n), where n represents the length of the key.
    public TrieNode<T> insert(String key, T data) {

        final StringBuilder sb = new StringBuilder();

        TrieNode<T> current = root;

        char[] chars = key.toCharArray();

        final char lastChar =  chars[chars.length - 1];

        boolean overrideHappened = false;

        for (char aChar : chars) {

            final boolean isLastChar =  aChar == lastChar;

            sb.append(aChar);

            final TrieNode<T> existing = current.getChildren().get(aChar);

            if (!overrideHappened
                    && isLastChar
                    && existing != null) {
                overrideHappened = true;
            }

            if (existing == null) {

                TrieNode<T> newChild = new TrieNode<>();
                newChild.setCompleteWord(false);
                newChild.setPrefix(sb.toString());

                current.getChildren().put(aChar, newChild);
            }

            current = current.getChildren().get(aChar);
        }

        // -------------------------------------------------------------------------------------------------------------

        final String s = sb.toString();

        if (overrideHappened) {
            if (current.getData() != null) {
                current.getData().triggerUpdateTime();
            }
            current.storeDataAsOld();
        }

        current.setCompleteWord(true);
        current.setPrefix(s);
        current.setData(data);
        data.setKey(s);

        return current;
    }


    // Note: The time complexity is O(n), where n represents the length of the key.
    public Optional<T> find(String key) {

        char[] chars = key.toCharArray();

        TrieNode<T> current = root;

        for (char aChar : chars) {

            TrieNode<T> existing = current.getChildren().get(aChar);
            if (existing != null) {
                current = existing;
            }
        }

        if (current.isCompleteWord() && current.getPrefix().equals(key)) {
            return Optional.of(current.getData());
        }
        return Optional.empty();

    }


    /*
        Note:

            Deletion of a key can be done lazily (by clearing just the value within the node corresponding to a key),
            or eagerly by cleaning up any parent nodes that are no longer necessary.

            Eager deletion is described/implemented in this method
     */
    @SuppressWarnings("unchecked")
    public Optional<T> delete(String key) {
        Object[] recordDeleted = new Object[]{null};
        _delete(root, key, 0, recordDeleted);
        return Optional.of(recordDeleted).map(r -> (T) r[0]);
    }

    private boolean _delete(TrieNode<T> current, String key, int index, Object[] recordDeleted) {

        /*
            Note:
                Clear the node corresponding to key[index], and delete the child key[index+1]
                if that subtrie is completely empty, and return whether `node` has been
                cleared.
         */

        if (key.length() == index) {

            current.setCompleteWord(false);
            recordDeleted[0] = current.getData();
            current.setData(null);

        } else {

            char c = key.charAt(index);
            TrieNode<T> existing = current.getChildren().get(c);

            if (existing != null) {
                boolean isSubtrieCompletelyEmpty = _delete(existing, key, index + 1, recordDeleted);

                if (isSubtrieCompletelyEmpty && EAGER_DELETION) {
                    current.getChildren().remove(c);
                }
            }
        }


        // -------------------------------------------------------------------------------------------------------------

        // Note: return true if and only if the currently examined subtrie is completely empty.
        return current.getData() == null
                && current.getChildren().isEmpty();
    }

    // Note: nice to have ===> delete with iteration [TODO]


    public List<T> keysWithPrefix(String prefix) {

        final LinkedList<T> results = new LinkedList<>();
        Optional<TrieNode<T>> node = getNode(root, prefix);

        _collect(node, results);

        return results;
    }

    private void _collect(Optional<TrieNode<T>> optNode, LinkedList<T> results) {

        if (optNode.isEmpty()) return;

        final TrieNode<T> trieNode = optNode.get();

        if (trieNode.getData() != null) {
            results.add(trieNode.getData());
        }

        final HashMap<Character, TrieNode<T>> children = trieNode.getChildren();
        for (Map.Entry<Character, TrieNode<T>> entry : children.entrySet()) {
            _collect(Optional.of(entry.getValue()), results);
        }
    }

    /*
        Note:
            Find node by key. This is the same as the `find` function defined above,
            but returning the found node itself rather than the found node's value.
     */
    public Optional<TrieNode<T>> getNode(TrieNode<T> current, String key) {

        for (char aChar : key.toCharArray()) {

            TrieNode<T> existing = current.getChildren().get(aChar);
            if (existing != null) {
                current = existing;
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(current);

    }


    public TrieStatistics<T> gatherStatisticsWithRecursion() {

        final List<TrieNode<T>> nodes = new LinkedList<>();
        final List<T> values = new LinkedList<>();

        int[] countOfNoCompleteWords = new int[]{0};
        int[] countOfCompleteWords = new int[]{0};
        int[] countOfCompleteWordsWithOldData = new int[]{0};

        _gatherStatistics(root, nodes, values, countOfNoCompleteWords, countOfCompleteWords, countOfCompleteWordsWithOldData);

        return new TrieStatistics<>(countOfNoCompleteWords[0], countOfCompleteWords[0], countOfCompleteWordsWithOldData[0], nodes, values);

    }

    // Note: nice to have ===> gatherStatisticsWithIteration [TODO]

    private void _gatherStatistics(TrieNode<T> current,
                                   List<TrieNode<T>> nodes, List<T> values,
                                   int[] countOfNoCompleteWords, int[] countOfCompleteWords, int[] countOfCompleteWordsWithOldData) {


        if (current.isCompleteWord()) {
            countOfCompleteWords[0] += 1;
            countOfCompleteWordsWithOldData[0] += current.getOldData().size();

            values.add(current.getData());
        } else {
            countOfNoCompleteWords[0] += 1;
        }


        HashMap<Character, TrieNode<T>> children = current.getChildren();
        for (TrieNode<T> value : children.values()) {
            _gatherStatistics(value, nodes, values, countOfNoCompleteWords, countOfCompleteWords, countOfCompleteWordsWithOldData);
        }

    }


    // ------

}
