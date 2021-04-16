package chriniko.kv.broker;

import chriniko.kv.broker.api.ConsistencyLevel;
import chriniko.kv.broker.api.KvServerContactPoint;
import chriniko.kv.broker.api.QueryKey;
import chriniko.kv.broker.error.response.ErrorReceivedFromKvServerException;
import chriniko.kv.broker.operation.KvBroker;
import chriniko.kv.datatypes.StringValue;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import chriniko.kv.protocol.NotOkayResponseException;
import chriniko.kv.server.infra.KvServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KvBrokerQueryOperationTest {


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
    void queryWorksAsExpected_whenNotEntryExists_NotFoundReply() throws Exception {

        // given (having started the servers)
        LinkedList<Integer> availablePorts = AvailablePortInfra.availablePorts(3);

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


        // =============================================================================================================
        // given
        kvBroker.put("chrinikoData",
                DatatypesAntlrParser.process("{ \"_studentDetails\" : [ { \"_username\" : \"chriniko\" } ; { \"_email\" : \"chriniko\" } ; { \"_address\" : [ { \"_street\" : \"Panepistimioupoli 123, Kesariani\" } ; { \"_postCode\" : \"16121\" } ; { \"_city\" : \"Athens\" } ; { \"_country\" : \"Greece\" } ] } ; { \"_name\" : [ { \"_firstname\" : \"Nikolaos\" } ; { \"_surname\" : \"Christidis\" } ] } ] }"),
                ConsistencyLevel.QUORUM);

        int occurrences = 0;
        final int quorum = ConsistencyLevel.calculateQuorum(replicationFactor);

        Value<?> v = kvServer1.getStorageEngine().fetch("chrinikoData");
        if (v != null) occurrences++;

        v = kvServer2.getStorageEngine().fetch("chrinikoData");
        if (v != null) occurrences++;

        v = kvServer3.getStorageEngine().fetch("chrinikoData");
        if (v != null) occurrences++;


        assertEquals(quorum, occurrences);


        Optional<Value<?>> searchResult = kvBroker.get("chrinikoData", ConsistencyLevel.QUORUM);
        assertTrue(searchResult.isPresent());



        // when
        Optional<Value<?>> queryResult = kvBroker.query("chrinikoData", QueryKey.build("_studentDetails", "_username"), ConsistencyLevel.ONE);



        // then
        assertTrue(queryResult.isPresent());

        Value<?> value = queryResult.get();
        assertTrue(value instanceof StringValue);

        StringValue stringValue = (StringValue) value;
        assertEquals("_username", stringValue.getKey());
        assertEquals("chriniko", stringValue.getValue());


        // clear
        kvBroker.stop();
        kvServer1.stop();
        kvServer2.stop();
        kvServer3.stop();

    }



}
