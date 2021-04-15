package chriniko.kv.broker;

import chriniko.kv.broker.api.KvServerContactPoint;
import chriniko.kv.broker.error.response.ErrorReceivedFromKvServerException;
import chriniko.kv.broker.infra.KvServerClient;
import chriniko.kv.broker.operation.KvBroker;
import chriniko.kv.protocol.NotOkayResponseException;
import chriniko.kv.server.infra.KvServer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class KvBrokerStartUpTest {

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
        LinkedList<Integer> availablePorts = AvailablePortInfra.availablePorts(3);

        int recordsInserted = 10;

        final CountDownLatch serversReady = new CountDownLatch(3);

        final KvServer kvServer1 = KvServer.create("server1");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer1.run("localhost", availablePorts.get(0), serversReady::countDown);
            } catch (IOException e) {
                fail(e);
            }
        }, workerPool);

        final KvServer kvServer2 = KvServer.create("server2");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer2.run("localhost", availablePorts.get(1), serversReady::countDown);
            } catch (IOException e) {
                fail(e);
            }
        }, workerPool);

        final KvServer kvServer3 = KvServer.create("server3");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer3.run("localhost", availablePorts.get(2), serversReady::countDown);
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
                                new KvServerContactPoint("server1", "localhost", availablePorts.get(0)),
                                new KvServerContactPoint("server2", "localhost", availablePorts.get(1)),
                                new KvServerContactPoint("server3", "localhost", availablePorts.get(2))
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

                    Set<Integer> expected = Set.of(availablePorts.get(0), availablePorts.get(1), availablePorts.get(2));
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

}
