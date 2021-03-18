package chriniko.kv.broker;

import chriniko.kv.protocol.NotOkayResponseException;
import chriniko.kv.server.KvServer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class KvBrokerTest {


    @Test
    void startWorksAsExpected() throws Exception {

        // given (having started the servers)
        final CountDownLatch serversReady = new CountDownLatch(3);

        final KvServer kvServer1 = KvServer.create("server1");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer1.run("localhost", 1711, () -> serversReady.countDown());
            } catch (IOException e) {
                fail(e);
            }
        });

        final KvServer kvServer2 = KvServer.create("server2");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer2.run("localhost", 1712, () -> serversReady.countDown());
            } catch (IOException e) {
                fail(e);
            }
        });

        final KvServer kvServer3 = KvServer.create("server3");
        CompletableFuture.runAsync(() -> {
            try {
                kvServer3.run("localhost", 1713, () -> serversReady.countDown());
            } catch (IOException e) {
                fail(e);
            }
        });


        boolean reachedZero = serversReady.await(15, TimeUnit.SECONDS);
        if (!reachedZero) fail("servers could not run!");


        // when (start the broker)
        final int replicationFactor = 2;
        final KvBroker kvBroker = new KvBroker();

        CompletableFuture.runAsync(() -> {
            try {
                kvBroker.start(
                        Arrays.asList(
                                new KvServerContactPoint("localhost", 1711),
                                new KvServerContactPoint("localhost", 1712),
                                new KvServerContactPoint("localhost", 1713)
                        ),

                        null,
                        replicationFactor
                );
            } catch (NotOkayResponseException | IOException e) {
                fail(e);
            }

        });


        // then

        // check connectivity
        Awaitility.await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    Map<KvServerContactPoint, KvServerClient> kvServerClients = kvBroker.getKvServerClientsByContactPoint();

                    System.out.println("NIAOU: " + kvServerClients);

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
                    if (records == 50) {
                        replicationsPerformed++;
                    }

                    records = kvServer2.getStorageEngine().totalRecords();
                    if (records == 50) {
                        replicationsPerformed++;
                    }

                    records = kvServer3.getStorageEngine().totalRecords();
                    if (records == 50) {
                        replicationsPerformed++;
                    }

                    assertEquals(replicationFactor, replicationsPerformed);
                });

    }


    @Test
    void getWorksAsExpected() {

        // given




        // when




        // then



    }

}