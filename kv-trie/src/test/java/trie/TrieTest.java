package trie;

import chriniko.kv.trie.Trie;
import chriniko.kv.trie.TrieEntry;
import chriniko.kv.trie.TrieNode;
import lombok.ToString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TrieTest {

    @Test
    void insertWorksAsExpected() {

        // given
        Trie<Record> trie = new Trie<>();


        // when
        trie.insert("bear", new Record());
        trie.insert("bell", new Record());
        trie.insert("bid", new Record());
        trie.insert("bull", new Record());
        trie.insert("buy", new Record());
        trie.insert("sell", new Record());
        trie.insert("stock", new Record());
        trie.insert("stop", new Record());


        // then
        System.out.println(trie);

        Trie<Record>.TrieStatistics trieStatistics = trie.gatherStatisticsWithRecursion();
        System.out.println(trieStatistics);

        assertEquals(
                Set.of("bear", "bell", "bid", "bull", "buy", "sell", "stock", "stop"),
                trieStatistics.getValues().stream().map(r -> r.key).collect(Collectors.toSet())
        );

        assertEquals(8, trieStatistics.getCountOfCompleteWords());
        assertEquals(14, trieStatistics.getCountOfNoCompleteWords());

    }


    @Test
    void findWorksAsExpected() {

        // given
        Trie<Record> trie = new Trie<>();

        trie.insert("bear", new Record());
        trie.insert("bell", new Record());

        TrieNode<Record> justInsertedNode = trie.insert("bid", new Record());
        assertEquals("bid", justInsertedNode.getData().key);
        assertNotNull(justInsertedNode.getData().value);
        assertEquals("bid", justInsertedNode.getPrefix());
        assertTrue(justInsertedNode.isCompleteWord());

        // when
        Optional<Record> r = trie.find("bear");

        // then
        Assertions.assertTrue(r.isPresent());
        Assertions.assertNotNull(r.get().value);


        // when
        r = trie.find("bea");

        // then
        Assertions.assertFalse(r.isPresent());


        // when
        r = trie.find("bid");

        // then
        Assertions.assertTrue(r.isPresent());
        Assertions.assertNotNull(r.get().value);

    }

    @Test
    void deleteWorksAsExpected() {

        // given
        Trie<Record> trie = new Trie<>();

        trie.insert("bear", new Record());
        trie.insert("bell", new Record());
        trie.insert("bid", new Record());
        trie.insert("bull", new Record());
        trie.insert("buy", new Record());
        trie.insert("sell", new Record());
        trie.insert("stock", new Record());
        trie.insert("stop", new Record());


        Trie<Record>.TrieStatistics trieStatistics = trie.gatherStatisticsWithRecursion();

        assertEquals(
                Set.of("bear", "bell", "bid", "bull", "buy", "sell", "stock", "stop"),
                trieStatistics.getValues().stream().map(r -> r.key).collect(Collectors.toSet())
        );

        assertEquals(8, trieStatistics.getCountOfCompleteWords());
        assertEquals(14, trieStatistics.getCountOfNoCompleteWords());


        // when
        Optional<Record> deleted = trie.delete("bear");


        // then
        Assertions.assertTrue(deleted.isPresent());
        Assertions.assertEquals("bear", deleted.get().key);

        Optional<Record> searchResult = trie.find("bear");
        Assertions.assertTrue(searchResult.isEmpty());


        // when
        deleted = trie.delete("bear");

        // then
        Assertions.assertTrue(deleted.isEmpty());

    }

    @Test
    void keysWithPrefixWorksAsExpected() {

        // given
        Trie<Record> trie = new Trie<>();

        trie.insert("bear", new Record());
        trie.insert("bell", new Record());
        trie.insert("bid", new Record());
        trie.insert("bull", new Record());
        trie.insert("buy", new Record());
        trie.insert("sell", new Record());
        trie.insert("stock", new Record());
        trie.insert("stop", new Record());


        Trie<Record>.TrieStatistics trieStatistics = trie.gatherStatisticsWithRecursion();

        assertEquals(
                Set.of("bear", "bell", "bid", "bull", "buy", "sell", "stock", "stop"),
                trieStatistics.getValues().stream().map(r -> r.key).collect(Collectors.toSet())
        );

        assertEquals(8, trieStatistics.getCountOfCompleteWords());
        assertEquals(14, trieStatistics.getCountOfNoCompleteWords());


        // when
        List<Record> records = trie.keysWithPrefix("b");


        // then
        Assertions.assertEquals(5, records.size());
        Set<String> actual = records.stream().map(r -> r.key).collect(Collectors.toSet());
        Set<String> expected = Set.of("bear", "bell", "bid", "bull", "buy");
        Assertions.assertEquals(expected, actual);


        // when
        records = trie.keysWithPrefix("bu");


        // then
        Assertions.assertEquals(2, records.size());
        actual = records.stream().map(r -> r.key).collect(Collectors.toSet());
        expected = Set.of("bull", "buy");
        Assertions.assertEquals(expected, actual);


        // when
        records = trie.keysWithPrefix("sto");


        // then
        Assertions.assertEquals(2, records.size());
        actual = records.stream().map(r -> r.key).collect(Collectors.toSet());
        expected = Set.of("stock", "stop");
        Assertions.assertEquals(expected, actual);


        // when
        records = trie.keysWithPrefix("gam");


        // then
        Assertions.assertEquals(0, records.size());
    }


    // --- infra ---
    @ToString
    static class Record implements TrieEntry<String> {
        private String key;
        private final String value = UUID.randomUUID().toString();

        private final Instant createTime = Instant.now();
        private Instant updateTime;

        @Override
        public String key() {
            return key;
        }

        @Override
        public void setKey(String k) {
            key = k;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public Instant createTime() {
            return createTime;
        }

        @Override
        public Instant updateTime() {
            return updateTime;
        }

        @Override
        public void triggerUpdateTime() {
            updateTime = Instant.now();
        }

        @Override
        public void indexContents() throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        public LinkedHashMap<String, String> getIndexedContentsByKeyPath() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Trie<?> getIndexedContentsByKeyPathTrie() {
            throw new UnsupportedOperationException();
        }

        public String getValue() {
            return value;
        }
    }
}