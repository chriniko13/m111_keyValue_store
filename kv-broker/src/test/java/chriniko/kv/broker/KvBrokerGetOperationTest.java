package chriniko.kv.broker;

import chriniko.kv.broker.error.response.ErrorReceivedFromKvServerException;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import chriniko.kv.protocol.NotOkayResponseException;
import chriniko.kv.server.KvServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class KvBrokerGetOperationTest {


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
                fail(e);
            }

        }, workerPool);


        reachedZero = brokerIsReady.await(15, TimeUnit.SECONDS);
        if (!reachedZero) fail("broker could not start");


        // given (add an entry with put operation)
        kvBroker.put("sample-key", DatatypesAntlrParser.process("{\"_name\": 123}"), ConsistencyLevel.ALL);
        Value<?> v = kvServer1.getStorageEngine().fetch("sample-key");
        assertNotNull(v);

        v = kvServer2.getStorageEngine().fetch("sample-key");
        assertNotNull(v);

        v = kvServer3.getStorageEngine().fetch("sample-key");
        assertNotNull(v);


        // =============================================================================================================
        // when
        Optional<Value<?>> result = kvBroker.get("sample-key", ConsistencyLevel.ONE);

        // then
        assertTrue(result.isPresent());
        assertEquals("{ \"_name\" : 123 }", result.get().asString());


        // =============================================================================================================
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


    // TODO get test consistency levels and if failures work as expected....use jacoco


    @Test
    void getWorksAsExpected_whenNotEntryExists_NotFoundReply() throws Exception {

        // given (having started the servers)
        final CountDownLatch serversReady = new CountDownLatch(3);

        final KvServer kvServer1 = KvServer.create("server1");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer1.run("localhost", 1744, serversReady::countDown);
            } catch (IOException e) {
                e.printStackTrace(System.err);
                fail(e);
            }
        }, workerPool);

        final KvServer kvServer2 = KvServer.create("server2");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer2.run("localhost", 1745, serversReady::countDown);
            } catch (IOException e) {
                e.printStackTrace(System.err);
                fail(e);
            }
        }, workerPool);

        final KvServer kvServer3 = KvServer.create("server3");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer3.run("localhost", 1746, serversReady::countDown);
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
                                new KvServerContactPoint("server1", "localhost", 1744),
                                new KvServerContactPoint("server2", "localhost", 1745),
                                new KvServerContactPoint("server3", "localhost", 1746)
                        ),
                        null,
                        false,
                        replicationFactor,
                        brokerIsReady::countDown
                );
            } catch (NotOkayResponseException | IOException | ErrorReceivedFromKvServerException e) {
                e.printStackTrace(System.err);
                fail(e);
            }

        }, workerPool);


        reachedZero = brokerIsReady.await(15, TimeUnit.SECONDS);
        if (!reachedZero) fail("broker could not start");


        // =============================================================================================================
        // when
        Optional<Value<?>> searchResult = kvBroker.get("someKey", ConsistencyLevel.ALL);

        // then
        assertFalse(searchResult.isPresent());


        // =============================================================================================================
        // given
        kvBroker.put("user01Data",
                DatatypesAntlrParser.process("{ \"_contents\" : [ { \"_level\" : 6 } ; { \"_street\" : \"0468 Shanda Harbors, South Earleneview, WI 26933-1884\" } ; { \"_name\" : \"dallas.marvin\" } ; { \"_age\" : 98 } ; { \"_height\" : 1.5102642 } ] }"),
                ConsistencyLevel.QUORUM);

        int occurrences = 0;
        final int quorum = ConsistencyLevel.calculateQuorum(replicationFactor);

        Value<?> v = kvServer1.getStorageEngine().fetch("user01Data");
        if (v != null) occurrences++;

        v = kvServer2.getStorageEngine().fetch("user01Data");
        if (v != null) occurrences++;

        v = kvServer3.getStorageEngine().fetch("user01Data");
        if (v != null) occurrences++;


        assertEquals(quorum, occurrences);


        // when
        searchResult = kvBroker.get("user01Data", ConsistencyLevel.QUORUM);

        // then
        assertTrue(searchResult.isPresent());


        // clear
        kvBroker.stop();
        kvServer1.stop();
        kvServer2.stop();
        kvServer3.stop();

    }

}
