
# Kv Trie

## Description

Contains implementations of Trie data-structure.

* [Trie.java](src/main/java/chriniko/kv/trie/Trie.java) is a no thread safe implementation

* [TrieLS.java](src/main/java/chriniko/kv/trie/lock_stripping/TrieLS.java) is a thread safe implementation by using lock stripping:
    ```text
        Brian Goetz - Java Concurrency in Practise - 11.4.3. Lock Striping
  
  
        Splitting a heavily contended lock into two is likely to result in two heavily contended locks. While this will produce a
        small scalability improvement by enabling two threads to execute concurrently instead of one, it still does not
  
        dramatically improve prospects for concurrency on a system with many processors. The lock splitting example in the
        ServerStatus classes does not offer any obvious opportunity for splitting the locks further.
        Lock splitting can sometimes be extended to partition locking on a variablesized set of independent objects, in which
        case it is called lock striping. For example, the implementation of ConcurrentHashMap uses an array of 16 locks, each of
        which guards 1/16 of the hash buckets; bucket N is guarded by lock N mod 16. Assuming the hash function provides
        reasonable spreading characteristics and keys are accessed uniformly, this should reduce the demand for any given lock
        by approximately a factor of 16. It is this technique that enables ConcurrentHashMap to support up to 16 concurrent
        writers. (The number of locks could be increased to provide even better concurrency under heavy access on highͲ
        processorͲcount systems, but the number of stripes should be increased beyond the default of 16 only when you have
        evidence that concurrent writers are generating enough contention to warrant raising the limit.)
        One of the downsides of lock striping is that locking the collection for exclusive access is more difficult and costly than
        with a single lock. Usually an operation can be performed by acquiring at most one lock, but occasionally you need to
        lock the entire collection, as when ConcurrentHashMap needs to expand the map and rehash the values into a larger set
        of buckets. This is typically done by acquiring all of the locks in the stripe set.
    ```

