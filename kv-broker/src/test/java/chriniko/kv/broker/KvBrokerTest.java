package chriniko.kv.broker;

import chriniko.kv.broker.error.response.ErrorReceivedFromKvServerException;
import chriniko.kv.datatypes.Value;
import chriniko.kv.protocol.NotOkayResponseException;
import chriniko.kv.server.KvServer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class KvBrokerTest {

    private ExecutorService workerPool;

    @BeforeEach
    void beforeEach() {
        workerPool = Executors.newCachedThreadPool();
    }

    @AfterEach
    void afterEach() {
        workerPool.shutdown();
    }


    @Test
    void startWorksAsExpected() throws Exception {

        // given (having started the servers)
        int recordsInserted = 10;

        final CountDownLatch serversReady = new CountDownLatch(3);

        final KvServer kvServer1 = KvServer.create("server1");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer1.run("localhost", 1711, serversReady::countDown);
            } catch (IOException e) {
                fail(e);
            }
        }, workerPool);

        final KvServer kvServer2 = KvServer.create("server2");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer2.run("localhost", 1712, serversReady::countDown);
            } catch (IOException e) {
                fail(e);
            }
        }, workerPool);

        final KvServer kvServer3 = KvServer.create("server3");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer3.run("localhost", 1713, serversReady::countDown);
            } catch (IOException e) {
                fail(e);
            }
        }, workerPool);


        boolean reachedZero = serversReady.await(15, TimeUnit.SECONDS);
        if (!reachedZero) fail("servers could not run!");


        // when (start the broker)
        final int replicationFactor = 2;
        final KvBroker kvBroker = new KvBroker();

        CompletableFuture.runAsync(() -> {
            try {
                kvBroker.start(
                        Arrays.asList(
                                new KvServerContactPoint("server1", "localhost", 1711),
                                new KvServerContactPoint("server2", "localhost", 1712),
                                new KvServerContactPoint("server3", "localhost", 1713)
                        ),
                        null,
                        true,
                        replicationFactor,
                        null
                );
            } catch (NotOkayResponseException | IOException | ErrorReceivedFromKvServerException e) {
                fail(e);
            }

        }, workerPool);


        // then

        // check connectivity
        Awaitility.await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    Map<KvServerContactPoint, KvServerClient> kvServerClients = kvBroker.getKvServerClientsByContactPoint();
                    System.out.println("kvServerClients: " + kvServerClients);

                    assertEquals(3, kvServerClients.size());

                    assertEquals(3, new HashSet<>(kvServerClients.values()).size());

                    Set<Integer> kvServerContactPoints = kvServerClients.keySet().stream()
                            .map(KvServerContactPoint::getPort)
                            .collect(Collectors.toSet());

                    Set<Integer> expected = Set.of(1711, 1712, 1713);
                    assertEquals(expected, kvServerContactPoints);


                    System.out.println("~~~~~~~~~~~~~~~~~~~");

                    kvServerClients.forEach((kvServerContactPoint, kvServerClient) -> {
                        System.out.println("kvServerContactPoint: " + kvServerContactPoint + " --- " + kvServerClient);
                    });
                });

        // check put has been executed successfully
        Awaitility.await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    int replicationsPerformed = 0;

                    int records = kvServer1.getStorageEngine().totalRecords();
                    System.out.println("RECORDS: " + records);
                    if (records == recordsInserted) {
                        replicationsPerformed++;
                    }

                    records = kvServer2.getStorageEngine().totalRecords();
                    if (records == recordsInserted) {
                        replicationsPerformed++;
                    }

                    records = kvServer3.getStorageEngine().totalRecords();
                    if (records == recordsInserted) {
                        replicationsPerformed++;
                    }

                    assertEquals(replicationFactor, replicationsPerformed);
                });


        // clear
        kvBroker.stop();
        kvServer1.stop();
        kvServer2.stop();
        kvServer3.stop();

    }


    @Test
    void putWorksAsExpected() throws Exception {

        // given (having started the servers)
        final CountDownLatch serversReady = new CountDownLatch(3);

        final KvServer kvServer1 = KvServer.create("server1");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer1.run("localhost", 1721, serversReady::countDown);
            } catch (IOException e) {
                fail(e);
            }
        }, workerPool);

        final KvServer kvServer2 = KvServer.create("server2");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer2.run("localhost", 1722, serversReady::countDown);
            } catch (IOException e) {
                fail(e);
            }
        }, workerPool);

        final KvServer kvServer3 = KvServer.create("server3");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer3.run("localhost", 1723, serversReady::countDown);
            } catch (IOException e) {
                fail(e);
            }
        }, workerPool);


        boolean reachedZero = serversReady.await(15, TimeUnit.SECONDS);
        if (!reachedZero) fail("servers could not run!");


        // given (start the broker)
        final int replicationFactor = 2;
        final KvBroker kvBroker = new KvBroker();

        kvBroker.start(
                Arrays.asList(
                        new KvServerContactPoint("server1", "localhost", 1721),
                        new KvServerContactPoint("server2", "localhost", 1722),
                        new KvServerContactPoint("server3", "localhost", 1723)
                ),
                null,
                false,
                replicationFactor,
                null
        );


        // when
        kvBroker.put("sample-key", "{\"_name\": 123}", ConsistencyLevel.ALL);


        // then
        Value<?> result = kvServer1.getStorageEngine().fetch("sample-key");
        assertNotNull(result);

        result = kvServer2.getStorageEngine().fetch("sample-key");
        assertNotNull(result);

        result = kvServer3.getStorageEngine().fetch("sample-key");
        assertNotNull(result);


        // when
        kvBroker.put("sample-key2", "{\"_name\": 123}", ConsistencyLevel.ONE);


        // then
        int occurrences = 0;
        result = kvServer1.getStorageEngine().fetch("sample-key2");
        if (result != null) {
            occurrences++;
        }

        result = kvServer2.getStorageEngine().fetch("sample-key2");
        if (result != null) {
            occurrences++;
        }

        result = kvServer3.getStorageEngine().fetch("sample-key2");
        if (result != null) {
            occurrences++;
        }

        assertEquals(1, occurrences);


        // when
        kvBroker.put("sample-key3", "{\"_name\": 123}", ConsistencyLevel.QUORUM);


        // then
        occurrences = 0;
        result = kvServer1.getStorageEngine().fetch("sample-key3");
        if (result != null) {
            occurrences++;
        }

        result = kvServer2.getStorageEngine().fetch("sample-key3");
        if (result != null) {
            occurrences++;
        }

        result = kvServer3.getStorageEngine().fetch("sample-key3");
        if (result != null) {
            occurrences++;
        }

        assertEquals(2, occurrences);


        // when
        kvBroker.put("sample-key4", "{\"_name\": 123}", ConsistencyLevel.REPLICATION_FACTOR);


        // then
        occurrences = 0;
        result = kvServer1.getStorageEngine().fetch("sample-key4");
        if (result != null) {
            occurrences++;
        }

        result = kvServer2.getStorageEngine().fetch("sample-key4");
        if (result != null) {
            occurrences++;
        }

        result = kvServer3.getStorageEngine().fetch("sample-key4");
        if (result != null) {
            occurrences++;
        }

        assertEquals(2, occurrences);


        // clear
        kvBroker.stop();
        kvServer1.stop();
        kvServer2.stop();
        kvServer3.stop();
    }


    // TODO put test consistency levels and if failures work as expected....

    // TODO get test consistency levels and if failures work as expected....


    @Test
    void getWorksAsExpected() throws Exception {

        // given (having started the servers)
        final CountDownLatch serversReady = new CountDownLatch(3);

        final KvServer kvServer1 = KvServer.create("server1");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer1.run("localhost", 1741, serversReady::countDown);
            } catch (IOException e) {
                e.printStackTrace(System.err);
                fail(e);
            }
        }, workerPool);

        final KvServer kvServer2 = KvServer.create("server2");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer2.run("localhost", 1742, serversReady::countDown);
            } catch (IOException e) {
                e.printStackTrace(System.err);
                fail(e);
            }
        }, workerPool);

        final KvServer kvServer3 = KvServer.create("server3");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer3.run("localhost", 1743, serversReady::countDown);
            } catch (IOException e) {
                e.printStackTrace(System.err);
                fail(e);
            }
        }, workerPool);


        boolean reachedZero = serversReady.await(15, TimeUnit.SECONDS);
        if (!reachedZero) fail("servers could not run!");


        // given (start the broker)
        final CountDownLatch brokerIsReady = new CountDownLatch(1);
        final int replicationFactor = 2;
        final KvBroker kvBroker = new KvBroker();

        CompletableFuture.runAsync(() -> {

            try {
                kvBroker.start(
                        Arrays.asList(
                                new KvServerContactPoint("server1", "localhost", 1741),
                                new KvServerContactPoint("server2", "localhost", 1742),
                                new KvServerContactPoint("server3", "localhost", 1743)
                        ),
                        null,
                        false,
                        replicationFactor,
                        brokerIsReady::countDown
                );
            } catch (NotOkayResponseException | IOException | ErrorReceivedFromKvServerException e) {
                e.printStackTrace(System.err);
            }

        }, workerPool);


        reachedZero = brokerIsReady.await(15, TimeUnit.SECONDS);
        if (!reachedZero) fail("broker could not start");


        // given (add an entry with put operation)
        kvBroker.put("sample-key", "{\"_name\": 123}", ConsistencyLevel.ALL);
        Value<?> v = kvServer1.getStorageEngine().fetch("sample-key");
        assertNotNull(v);

        v = kvServer2.getStorageEngine().fetch("sample-key");
        assertNotNull(v);

        v = kvServer3.getStorageEngine().fetch("sample-key");
        assertNotNull(v);


        // when
        Optional<String> result = kvBroker.get("sample-key", ConsistencyLevel.ONE);

        // then
        assertTrue(result.isPresent());
        assertEquals("{ \"_name\" : 123 }", result.get());


        // when
        result = kvBroker.get("sample-key-fooBar", ConsistencyLevel.ONE);

        // then
        assertFalse(result.isPresent());


        // clear
        kvBroker.stop();
        kvServer1.stop();
        kvServer2.stop();
        kvServer3.stop();
    }

}