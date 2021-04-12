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
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class KvBrokerDeleteOperationTest {


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
    void deleteWorksAsExpected() throws Exception {
        LinkedList<Integer> availablePorts = AvailablePortInfra.availablePorts(3);

        // given (having started the servers)
        final CountDownLatch serversReady = new CountDownLatch(3);

        final KvServer kvServer1 = KvServer.create("server1");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer1.run("localhost", availablePorts.get(0), serversReady::countDown);
            } catch (IOException e) {
                e.printStackTrace(System.err);
                fail(e);
            }
        }, workerPool);

        final KvServer kvServer2 = KvServer.create("server2");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer2.run("localhost", availablePorts.get(1), serversReady::countDown);
            } catch (IOException e) {
                e.printStackTrace(System.err);
                fail(e);
            }
        }, workerPool);

        final KvServer kvServer3 = KvServer.create("server3");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer3.run("localhost", availablePorts.get(2), serversReady::countDown);
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
                                new KvServerContactPoint("server1", "localhost", availablePorts.get(0)),
                                new KvServerContactPoint("server2", "localhost", availablePorts.get(1)),
                                new KvServerContactPoint("server3", "localhost", availablePorts.get(2))
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
        Optional<Value<?>> result = kvBroker.delete("sample-key", ConsistencyLevel.ONE);

        // then
        assertTrue(result.isPresent());
        assertEquals("{ \"_name\" : 123 }", result.get().asString());


        // =============================================================================================================
        // when
        result = kvBroker.delete("sample-key", ConsistencyLevel.ALL);

        // then
        assertTrue(result.isPresent());
        assertEquals("{ \"_name\" : 123 }", result.get().asString());


        // =============================================================================================================
        // when
        result = kvBroker.delete("sample-key", ConsistencyLevel.ALL);

        // then
        assertFalse(result.isPresent());


        // =============================================================================================================
        // when
        result = kvBroker.delete("foo-bar", ConsistencyLevel.ALL);

        // then
        assertFalse(result.isPresent());


        // =============================================================================================================
        // given
        kvBroker.put("simpleUserName001", DatatypesAntlrParser.process("{\"_name\": 123}"), ConsistencyLevel.ALL);

        v = kvServer1.getStorageEngine().fetch("simpleUserName001");
        assertNotNull(v);

        v = kvServer2.getStorageEngine().fetch("simpleUserName001");
        assertNotNull(v);

        v = kvServer3.getStorageEngine().fetch("simpleUserName001");
        assertNotNull(v);


        // when
        result = kvBroker.delete("simpleUserName001", ConsistencyLevel.QUORUM);

        // then
        assertTrue(result.isPresent());
        assertEquals("{ \"_name\" : 123 }", result.get().asString());




        // clear
        kvBroker.stop();
        kvServer1.stop();
        kvServer2.stop();
        kvServer3.stop();
    }




}
