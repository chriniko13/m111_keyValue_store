package chriniko.kv.broker;

import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import chriniko.kv.server.KvServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KvBrokerPutOperationTest {

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



        // =====================================================================================================================
        // when
        kvBroker.put("sample-key", DatatypesAntlrParser.process("{\"_name\": 123}"), ConsistencyLevel.ALL);


        // then
        Value<?> result = kvServer1.getStorageEngine().fetch("sample-key");
        assertNotNull(result);

        result = kvServer2.getStorageEngine().fetch("sample-key");
        assertNotNull(result);

        result = kvServer3.getStorageEngine().fetch("sample-key");
        assertNotNull(result);



        // =====================================================================================================================
        // when
        kvBroker.put("sample-key2", DatatypesAntlrParser.process("{\"_name\": 123}"), ConsistencyLevel.ONE);


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



        // =====================================================================================================================
        // when
        kvBroker.put("sample-key3", DatatypesAntlrParser.process("{\"_name\": 123}"), ConsistencyLevel.QUORUM);


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



        // =====================================================================================================================
        // when
        kvBroker.put("sample-key4", DatatypesAntlrParser.process("{\"_name\": 123}"), ConsistencyLevel.REPLICATION_FACTOR);


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



    // TODO put test consistency levels and if failures work as expected....use jacoco


    // TODO parsing error.....



}
