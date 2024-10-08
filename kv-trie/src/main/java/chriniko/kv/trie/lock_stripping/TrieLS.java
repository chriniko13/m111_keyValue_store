package chriniko.kv.trie.lock_stripping;

import chriniko.kv.trie.error.ReadLockAcquireFailureException;
import chriniko.kv.trie.error.StaleDataOperationException;
import chriniko.kv.trie.error.WriteLockAcquireFailureException;
import chriniko.kv.trie.infra.TrieEntry;
import chriniko.kv.trie.infra.TrieStatistics;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class TrieLS<T extends TrieEntry<?>> {

    private final int readLockTimeoutMs;
    private final int writeLockTimeoutMs ;

    private static final boolean EAGER_DELETION = true;

    private final TrieNodeLS<T> root;

    public TrieLS(int readLockTimeoutMs, int writeLockTimeoutMs) {
        this.readLockTimeoutMs = readLockTimeoutMs;
        this.writeLockTimeoutMs = writeLockTimeoutMs;

        root = new TrieNodeLS<>();
        root.setPrefix("");
        root.setCompleteWord(false);
    }

    public void clear() throws WriteLockAcquireFailureException {

        // grab write lock
        final Lock writeLock = root.writeLock();
        boolean writeLockAcquired;
        try {
            writeLockAcquired = writeLock.tryLock(writeLockTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        if (!writeLockAcquired) {
            throw new WriteLockAcquireFailureException("could not grab write lock");
        }

        try {
            // process
            root.clear();

            System.gc(); // give hint for gc process

        } finally {
            // release write lock
            writeLock.unlock();
        }
    }


    // Note: The time complexity is O(n), where n represents the length of the key.
    public TrieNodeLS<T> insert(String key, T data) throws ReadLockAcquireFailureException, WriteLockAcquireFailureException, StaleDataOperationException {

        final StringBuilder sb = new StringBuilder();

        TrieNodeLS<T> current = root;

        final char[] chars = key.toCharArray();

        boolean overrideHappened = false;

        for (int i=0; i<chars.length; i++) {

            final char aChar = chars[i];

            final boolean isLastChar =  i == chars.length - 1;

            sb.append(aChar);

            // grab write lock
            boolean writeLockAcquired;
            final Lock writeLock = current.writeLock();
            try {
                writeLockAcquired = writeLock.tryLock(writeLockTimeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            if (!writeLockAcquired) {
                throw new WriteLockAcquireFailureException("could not grab write lock");
            }

            try {

                // process
                final TrieNodeLS<T> existing = current.getChildren().get(aChar);

                if (isLastChar
                        && existing != null) {
                    overrideHappened = true;
                }


                if (existing == null) {
                    // construct new node to be inserted
                    final TrieNodeLS<T> newChild = new TrieNodeLS<>();
                    newChild.setCompleteWord(false);
                    newChild.setPrefix(sb.toString());


                    // modify
                    current.getChildren().put(aChar, newChild);
                }

                // walk down
                current = current.getChildren().get(aChar);


                if (isLastChar) {
                    final String s = sb.toString();

                    // modify
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

            } finally {
                // release write lock
                writeLock.unlock();
            }


        } // for.

        throw new IllegalStateException();
    }


    // Note: The time complexity is O(n), where n represents the length of the key.
    public Optional<T> find(String key) throws ReadLockAcquireFailureException {

        final char[] chars = key.toCharArray();

        TrieNodeLS<T> current = root;

        for (int i = 0; i<chars.length; i++) {

            char aChar = chars[i];
            final boolean isLastChar =  i == chars.length - 1;

            // grab read lock
            boolean readLockAcquired;
            final Lock readLock = current.readLock();
            try {
                readLockAcquired = readLock.tryLock(readLockTimeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            if (!readLockAcquired) {
                throw new ReadLockAcquireFailureException("could not grab read lock");
            }

            try {

                // walk down if feasible
                TrieNodeLS<T> existing = current.getChildren().get(aChar);
                if (existing != null) {
                    current = existing;
                }

                if (isLastChar) {

                    // calculate result
                    if (current.isCompleteWord() && current.getPrefix().equals(key)) {
                        return Optional.of(current.getData());
                    }
                    return Optional.empty();
                }

            } finally {
                // release read lock
                readLock.unlock();
            }

        } // for.

        throw new IllegalStateException();
    }


    /*
        Note:

            Deletion of a key can be done lazily (by clearing just the value within the node corresponding to a key),
            or eagerly by cleaning up any parent nodes that are no longer necessary.

            Eager deletion is described/implemented in this method
     */
    @SuppressWarnings("unchecked")
    public Optional<T> delete(String key) throws WriteLockAcquireFailureException, ReadLockAcquireFailureException, StaleDataOperationException {
        Object[] recordDeleted = new Object[]{null};
        _delete(root, key, 0, recordDeleted);
        return Optional.of(recordDeleted).map(r -> (T) r[0]);
    }

    private boolean _delete(TrieNodeLS<T> current, String key, int index, Object[] recordDeleted) throws WriteLockAcquireFailureException, ReadLockAcquireFailureException, StaleDataOperationException {

        /*
            Note:
                Clear the node corresponding to key[index], and delete the child key[index+1]
                if that subtrie is completely empty, and return whether `node` has been
                cleared.
         */

        if (key.length() == index) {

            // acquire write lock
            final Lock writeLock = current.writeLock();
            boolean writeLockAcquired;
            try {
                writeLockAcquired = writeLock.tryLock(writeLockTimeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            if (!writeLockAcquired) {
                throw new WriteLockAcquireFailureException("could not grab write lock");
            }


            try {
                // modify
                current.setCompleteWord(false);
                recordDeleted[0] = current.getData();
                current.setData(null);
                if (!current.getChildren().isEmpty()) {
                    throw new IllegalStateException();
                }
                return true;

            } finally {

                // release write lock
                writeLock.unlock();
            }


        } else {

            final char c = key.charAt(index);

            // grab write lock
            boolean writeLockAcquired;
            final Lock writeLock = current.writeLock();
            try {
                writeLockAcquired = writeLock.tryLock(writeLockTimeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            if (!writeLockAcquired) {
                throw new WriteLockAcquireFailureException("could not grab write lock");
            }

            try {

                // process
                final TrieNodeLS<T> existing = current.getChildren().get(c);
                if (existing != null) {

                    final boolean isSubtrieCompletelyEmpty = _delete(existing, key, index + 1, recordDeleted);

                    if (isSubtrieCompletelyEmpty && EAGER_DELETION) {
                        current.getChildren().remove(c);
                    }

                }

                // on exit of recursion...perform...

                // Note: return true if and only if the currently examined subtrie is completely empty.
                return current.getData() == null
                        && current.getChildren().isEmpty();

            } finally {

                // release write lock
                writeLock.unlock();
            }
        }

    }

    // Note: nice to have ===> delete with iteration [TODO]



    public List<T> keysWithPrefix(String prefix) throws ReadLockAcquireFailureException {

        final LinkedList<T> results = new LinkedList<>();
        Optional<TrieNodeLS<T>> node = getNode(root, prefix);

        _collect(node, results);

        return results;
    }

    private void _collect(Optional<TrieNodeLS<T>> optNode, LinkedList<T> results) throws ReadLockAcquireFailureException {

        if (optNode.isEmpty()) return;

        final TrieNodeLS<T> trieNode = optNode.get();


        // acquire read lock
        boolean readLockAcquired;
        final Lock readLock = trieNode.readLock();
        try {
            readLockAcquired = readLock.tryLock(readLockTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        if (!readLockAcquired) {
            throw new ReadLockAcquireFailureException("could not grab read lock");
        }


        try {
            // process
            if (trieNode.getData() != null) {
                results.add(trieNode.getData());
            }


            final HashMap<Character, TrieNodeLS<T>> children = trieNode.getChildren();
            for (Map.Entry<Character, TrieNodeLS<T>> entry : children.entrySet()) {
                _collect(Optional.of(entry.getValue()), results);
            }

        } finally {
            // release read lock
            readLock.unlock();
        }
    }

    /*
        Note:
            Find node by key. This is the same as the `find` function defined above,
            but returning the found node itself rather than the found node's value.
     */
    public Optional<TrieNodeLS<T>> getNode(TrieNodeLS<T> current, String key) throws ReadLockAcquireFailureException {

        for (char aChar : key.toCharArray()) {

            // grab read lock
            boolean readLockAcquired;
            final Lock readLock = current.readLock();
            try {
                readLockAcquired = readLock.tryLock(readLockTimeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            if (!readLockAcquired) {
                throw new ReadLockAcquireFailureException("could not grab read lock");
            }

            try {
                // process
                final TrieNodeLS<T> existing = current.getChildren().get(aChar);
                if (existing != null) {
                    current = existing;
                } else {
                    return Optional.empty();
                }

            } finally {
                // release read lock
                readLock.unlock();

            }
        }

        return Optional.of(current);

    }


    public TrieStatistics<T> gatherStatisticsWithRecursion() throws ReadLockAcquireFailureException {

        final List<TrieNodeLS<T>> nodes = new LinkedList<>();
        final List<T> values = new LinkedList<>();

        int[] countOfNoCompleteWords = new int[]{0};
        int[] countOfCompleteWords = new int[]{0};
        int[] countOfOldData = new int[]{0};

        _gatherStatistics(root, nodes, values, countOfNoCompleteWords, countOfCompleteWords, countOfOldData);

        return new TrieStatistics<>(countOfNoCompleteWords[0], countOfCompleteWords[0], countOfOldData[0], nodes, values);

    }

    // Note: nice to have ===> gatherStatisticsWithIteration [TODO]

    private void _gatherStatistics(TrieNodeLS<T> current,
                                   List<TrieNodeLS<T>> nodes, List<T> values,
                                   int[] countOfNoCompleteWords, int[] countOfCompleteWords, int[] countOfOldData) throws ReadLockAcquireFailureException {

        // grab read lock
        boolean readLockAcquired;
        final Lock readLock = current.readLock();
        try {
            readLockAcquired = readLock.tryLock(readLockTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        if (!readLockAcquired) {
            throw new ReadLockAcquireFailureException("could not grab read lock");
        }


        try {

            // process
            if (current.isCompleteWord()) {
                countOfCompleteWords[0] += 1;
                countOfOldData[0] += current.getOldData().size();

                nodes.add(current);
                values.add(current.getData());
            } else {
                countOfNoCompleteWords[0] += 1;
            }


            final HashMap<Character, TrieNodeLS<T>> children = current.getChildren();
            for (TrieNodeLS<T> value : children.values()) {
                _gatherStatistics(value, nodes, values, countOfNoCompleteWords, countOfCompleteWords, countOfOldData);
            }


        } finally {
            // release read lock
            //System.out.println("releasing: " + readLock.hashCode());
            readLock.unlock();
        }

    }





    // ------------- quick testing ------------------

    public static void main(String[] args) {
        caller();
    }

    private static void foo(int n) {
        System.out.println("n is " + n);

        if (n != 0) {
            foo (n - 1);
        }
    }

    public static void caller() {
        try {
            foo(4);
        } finally {
            System.out.println("finally");
        }


        for (int i=0; i<10; i++) {


            try {
                System.out.println(i);
            }finally {
                System.out.println("fin");
            }
        }
    }





}
