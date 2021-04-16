package trie;

import chriniko.kv.trie.Trie;
import chriniko.kv.trie.TrieNode;
import chriniko.kv.trie.TrieStatistics;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
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

        TrieStatistics<Record> trieStatistics = trie.<Record>gatherStatisticsWithRecursion();
        System.out.println(trieStatistics);

        assertEquals(
                Set.of("bear", "bell", "bid", "bull", "buy", "sell", "stock", "stop"),
                trieStatistics.getValues().stream().map(Record::key).collect(Collectors.toSet())
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
        assertEquals("bid", justInsertedNode.getData().key());
        assertNotNull(justInsertedNode.getData().value());
        assertEquals("bid", justInsertedNode.getPrefix());
        assertTrue(justInsertedNode.isCompleteWord());

        // when
        Optional<Record> r = trie.find("bear");

        // then
        Assertions.assertTrue(r.isPresent());
        Assertions.assertNotNull(r.get().value());


        // when
        r = trie.find("bea");

        // then
        Assertions.assertFalse(r.isPresent());


        // when
        r = trie.find("bid");

        // then
        Assertions.assertTrue(r.isPresent());
        Assertions.assertNotNull(r.get().value());

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


        TrieStatistics<Record> trieStatistics = trie.gatherStatisticsWithRecursion();

        assertEquals(
                Set.of("bear", "bell", "bid", "bull", "buy", "sell", "stock", "stop"),
                trieStatistics.getValues().stream().map(r -> r.key()).collect(Collectors.toSet())
        );

        assertEquals(8, trieStatistics.getCountOfCompleteWords());
        assertEquals(14, trieStatistics.getCountOfNoCompleteWords());


        // when
        Optional<Record> deleted = trie.delete("bear");


        // then
        Assertions.assertTrue(deleted.isPresent());
        Assertions.assertEquals("bear", deleted.get().key());

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


        TrieStatistics<Record> trieStatistics = trie.gatherStatisticsWithRecursion();

        assertEquals(
                Set.of("bear", "bell", "bid", "bull", "buy", "sell", "stock", "stop"),
                trieStatistics.getValues().stream().map(r -> r.key()).collect(Collectors.toSet())
        );

        assertEquals(8, trieStatistics.getCountOfCompleteWords());
        assertEquals(14, trieStatistics.getCountOfNoCompleteWords());


        // when
        List<Record> records = trie.keysWithPrefix("b");


        // then
        Assertions.assertEquals(5, records.size());
        Set<String> actual = records.stream().map(r -> r.key()).collect(Collectors.toSet());
        Set<String> expected = Set.of("bear", "bell", "bid", "bull", "buy");
        Assertions.assertEquals(expected, actual);


        // when
        records = trie.keysWithPrefix("bu");


        // then
        Assertions.assertEquals(2, records.size());
        actual = records.stream().map(r -> r.key()).collect(Collectors.toSet());
        expected = Set.of("bull", "buy");
        Assertions.assertEquals(expected, actual);


        // when
        records = trie.keysWithPrefix("sto");


        // then
        Assertions.assertEquals(2, records.size());
        actual = records.stream().map(r -> r.key()).collect(Collectors.toSet());
        expected = Set.of("stock", "stop");
        Assertions.assertEquals(expected, actual);


        // when
        records = trie.keysWithPrefix("gam");


        // then
        Assertions.assertEquals(0, records.size());


        // when
        trie.clear();

        // then
        trieStatistics = trie.gatherStatisticsWithRecursion();
        assertEquals(0, trieStatistics.getCountOfCompleteWords());
        assertEquals(1, trieStatistics.getCountOfNoCompleteWords());
        assertTrue(trieStatistics.getValues().isEmpty());
    }

    @Test
    void noThreadSafeProtection() {

        // given
        final int threads = Runtime.getRuntime().availableProcessors() * 10;
        System.out.println("threads: " + threads);
        final ExecutorService workers = Executors.newFixedThreadPool(threads);

        final int totalRuns = 5;
        final int opsPerWriter = 30;

        final Trie<Record> trie = new Trie<>();

        final int writers = threads - 4;
        final CyclicBarrier rendezvous = new CyclicBarrier(writers);
        System.out.println("writers: " + writers);

        final CountDownLatch workFinished = new CountDownLatch(writers);

        final Runnable writer = () -> {

            // rendezvous
            try {
                rendezvous.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail(e);
            } catch (BrokenBarrierException | TimeoutException e) {
                fail(e);
            }


            // actual work
            for (int i =1; i<=opsPerWriter; i++) {

                final String fullName = Faker.instance().name().fullName();
                final String[] names = fullName.split(" ");

                for (String name : names) {
                    Record record = new Record();
                    record.setKey(name);

                    trie.insert(name, record);
                }
            }

            workFinished.countDown();
        };


        boolean atLeastOneFail = false;
        for (int i=1; i<=totalRuns; i++) {

            if (atLeastOneFail) {
                System.out.println("at least one fail...exiting");
                break;
            }

            trie.clear();
            System.out.println("run: " + i);


            // when
            for(int k =1; k<=writers; k++) {
                workers.submit(writer);
            }



            // then
            try {
                boolean reachedZero = workFinished.await(15, TimeUnit.SECONDS);
                if (!reachedZero) {
                    fail("!reachedZero");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail(e);
            }

            int totalEntries = writers * opsPerWriter;
            TrieStatistics<Record> trieStatistics = trie.gatherStatisticsWithRecursion();

            System.out.println("totalEntries: " + totalEntries + " --- trieStatistics.getCountOfCompleteWords(): " + trieStatistics.getCountOfCompleteWords());

            if (totalEntries != trieStatistics.getCountOfCompleteWords()) {
                atLeastOneFail = true;
            }

        }

        assertTrue(atLeastOneFail);

        // clear
        workers.shutdown();
    }

}