package trie;

import chriniko.kv.trie.Trie;
import chriniko.kv.trie.TrieNode;
import chriniko.kv.trie.infra.TrieStatistics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TrieTest {


    @Test
    void insertOverrideWorksAsExpected() {

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


        // when
        TrieNode<Record> trieNode = trie.insert("bear", new Record());


        // then
        List<Record> oldData = trieNode.getOldData();
        assertEquals(1, oldData.size());
        assertNotNull(oldData.get(0).updateTime());



        TrieStatistics<Record> trieStatistics = trie.<Record>gatherStatisticsWithRecursion();
        System.out.println(trieStatistics);

        assertEquals(
                Set.of("bear", "bell", "bid", "bull", "buy", "sell", "stock", "stop"),
                trieStatistics.getValues().stream().map(Record::key).collect(Collectors.toSet())
        );

        assertEquals(8, trieStatistics.getCountOfCompleteWords());
        assertEquals(1, trieStatistics.getCountOfOldData());
        assertEquals(14, trieStatistics.getCountOfNoCompleteWords());
    }


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

        trie.insert("bear", new Record());


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


    // --- multi threading test ---

    @Test
    void noThreadSafeProtection() {

        // given
        boolean atLeastOneAssertionFailed = false;

        Thread junitThread = Thread.currentThread();

        final int threads = Runtime.getRuntime().availableProcessors() * 10;
        System.out.println("threads: " + threads);

        final ExecutorService workers = Executors.newCachedThreadPool();

        final int totalRuns = 200;

        final Trie<Record> trie = new Trie<>();

        final int writers = threads / 2;
        System.out.println("writers: " + writers);


        final AtomicReference<CyclicBarrier> rendezvousRef = new AtomicReference<>();
        final AtomicReference<CountDownLatch> workFinishedRef = new AtomicReference<>();

        final String idToInsert = UUID.randomUUID().toString();
        final int insertsToPerform = 15;

        final Runnable writer = () -> {

            try {
                // rendezvous
                try {
                    rendezvousRef.get().await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                    Thread.currentThread().interrupt();
                    fail(e);
                    junitThread.interrupt();
                } catch (BrokenBarrierException | TimeoutException e) {
                    e.printStackTrace(System.err);
                    fail(e);
                    junitThread.interrupt();
                }


                // actual work
                for (int op = 1; op<=insertsToPerform; op++) {
                    Record record = new Record();
                    record.setKey(idToInsert);

                    trie.insert(idToInsert, record);
                }

            } catch (Exception e) {
                e.printStackTrace(System.err);
                fail(e);
                junitThread.interrupt();

            } finally {
                //System.out.println(Thread.currentThread().getName() + " FINISHED WORK");
                workFinishedRef.get().countDown();
            }
        };


        for (int i=1; i<=totalRuns; i++) {


            rendezvousRef.set(new CyclicBarrier(writers, () -> System.out.println("LET'S ROLL!!!")));
            workFinishedRef.set(new CountDownLatch(writers));

            trie.clear();
            System.out.println("\n\nrun: " + i);


            // when
            for(int k =1; k<=writers; k++) {
                workers.submit(writer);
            }



            // then
            try {
                boolean reachedZero = workFinishedRef.get().await(30, TimeUnit.SECONDS);
                if (!reachedZero) {
                    fail("!reachedZero");
                }
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
                Thread.currentThread().interrupt();
                fail(e);
                junitThread.interrupt();
            }

            final TrieStatistics<Record> trieStatistics = trie.gatherStatisticsWithRecursion();


            int countOfCompleteWordsWithOldData = trieStatistics.getCountOfOldData();
            System.out.println("countOfCompleteWordsWithOldData: " + countOfCompleteWordsWithOldData);


            int countOfCompleteWords = trieStatistics.getCountOfCompleteWords();
            System.out.println("countOfCompleteWords: " + countOfCompleteWords);


            int actualTotal = countOfCompleteWords + countOfCompleteWordsWithOldData;
            System.out.println("actualTotal: " + actualTotal);


            int expectedTotal = writers * insertsToPerform;
            System.out.println("expectedTotal: " + expectedTotal);

            if (actualTotal != expectedTotal) {
                atLeastOneAssertionFailed = true;
            }

        }


        assertTrue(atLeastOneAssertionFailed);

        // clear
        workers.shutdown();
    }

}