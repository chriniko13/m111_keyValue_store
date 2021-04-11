package chriniko.kv.broker;

import chriniko.kv.protocol.Operations;
import chriniko.kv.protocol.ProtocolConstants;
import org.awaitility.Awaitility;
import org.javatuples.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CheckKvServersHealthWorkerTest {

    @Mock
    private KvServerClient kvServerClient;

    @Mock
    private KvServerClient kvServerClient2;

    @Mock
    private KvServerClient kvServerClient3;

    @Test
    void workerWorksAsExpected() throws Exception {

        // given
        final ConcurrentHashMap<KvServerContactPoint, KvServerClient> kvServerClientsByContactPoint
                = new ConcurrentHashMap<>();
        kvServerClientsByContactPoint.put(new KvServerContactPoint("server1", "localhost", 8090), kvServerClient);
        kvServerClientsByContactPoint.put(new KvServerContactPoint("server2", "localhost", 8091), kvServerClient2);
        kvServerClientsByContactPoint.put(new KvServerContactPoint("server3", "localhost", 8092), kvServerClient3);


        Mockito.when(kvServerClient.sendMessage(Operations.HEALTH_CHECK.getMsgOp()))
                .thenReturn(ProtocolConstants.OKAY_RESP);
        Mockito.when(kvServerClient.makeCopy()).thenReturn(kvServerClient);

        Mockito.when(kvServerClient2.sendMessage(Operations.HEALTH_CHECK.getMsgOp()))
                .thenReturn("BOOM");
        Mockito.when(kvServerClient2.makeCopy()).thenReturn(kvServerClient2);

        Mockito.when(kvServerClient3.sendMessage(Operations.HEALTH_CHECK.getMsgOp()))
                .thenThrow(ClosedChannelException.class);
        Mockito.when(kvServerClient3.makeCopy()).thenReturn(kvServerClient3);

        int replicationFactor = 2;
        AtomicBoolean replicationThresholdSatisfied = new AtomicBoolean();

        final ConcurrentHashMap<KvServerContactPoint, KvServerHealthStateStats> kvServerHealthStateStatsByContactPoint
                = new ConcurrentHashMap<>();

        final ConcurrentHashMap<KvServerHealthState, Set<Pair<KvServerClient, KvServerContactPoint>>> kvServerClientsByServerHealthStateStats
                = new ConcurrentHashMap<>();

        final CheckKvServersHealthWorker w
                = new CheckKvServersHealthWorker(100,

                replicationFactor, replicationThresholdSatisfied,

                kvServerClientsByContactPoint, kvServerHealthStateStatsByContactPoint, kvServerClientsByServerHealthStateStats);


        // when
        w.start();


        // then
        Awaitility.await()
                .atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> {


                    assertEquals(3, kvServerHealthStateStatsByContactPoint.size());

                    Set<KvServerHealthState> actual =
                            kvServerHealthStateStatsByContactPoint.values().stream()
                                    .map(KvServerHealthStateStats::getHealthState)
                                    .collect(Collectors.toSet());

                    HashSet<KvServerHealthState> expected = new HashSet<>();
                    expected.add(KvServerHealthState.UP);
                    expected.add(KvServerHealthState.NOT_OKAY);
                    expected.add(KvServerHealthState.DOWN);

                    assertEquals(expected, actual);


                    Set<KvServerHealthState> s = new HashSet<>(kvServerClientsByServerHealthStateStats.keySet());
                    assertEquals(s, actual);

                    for (Set<Pair<KvServerClient, KvServerContactPoint>> value : kvServerClientsByServerHealthStateStats.values()) {
                        assertEquals(1, value.size());

                        Iterator<Pair<KvServerClient, KvServerContactPoint>> iter = value.iterator();
                        Pair<KvServerClient, KvServerContactPoint> r = iter.next();
                        assertNotNull(r.getValue0());
                        assertNotNull(r.getValue1());

                    }

                });
    }


    @Test
    void workerMaintainsReplicationFactorIfSatisfiedStateWorksAsExpected() throws Exception {

        // given
        final ConcurrentHashMap<KvServerContactPoint, KvServerClient> kvServerClientsByContactPoint
                = new ConcurrentHashMap<>();
        kvServerClientsByContactPoint.put(new KvServerContactPoint("server1", "localhost", 8090), kvServerClient);
        kvServerClientsByContactPoint.put(new KvServerContactPoint("server2", "localhost", 8091), kvServerClient2);
        kvServerClientsByContactPoint.put(new KvServerContactPoint("server3", "localhost", 8092), kvServerClient3);


        Mockito.when(kvServerClient.sendMessage(Operations.HEALTH_CHECK.getMsgOp()))
                .thenReturn(ProtocolConstants.OKAY_RESP);
        Mockito.when(kvServerClient.makeCopy()).thenReturn(kvServerClient);

        Mockito.when(kvServerClient2.sendMessage(Operations.HEALTH_CHECK.getMsgOp()))
                .thenReturn(ProtocolConstants.OKAY_RESP);
        Mockito.when(kvServerClient2.makeCopy()).thenReturn(kvServerClient2);

        Mockito.when(kvServerClient3.sendMessage(Operations.HEALTH_CHECK.getMsgOp()))
                .thenThrow(ClosedChannelException.class);
        Mockito.when(kvServerClient3.makeCopy()).thenReturn(kvServerClient3);

        int replicationFactor = 2;
        AtomicBoolean replicationThresholdSatisfied = new AtomicBoolean();

        final ConcurrentHashMap<KvServerContactPoint, KvServerHealthStateStats> kvServerHealthStateStatsByContactPoint
                = new ConcurrentHashMap<>();

        final ConcurrentHashMap<KvServerHealthState, Set<Pair<KvServerClient, KvServerContactPoint>>> kvServerClientsByServerHealthStateStats
                = new ConcurrentHashMap<>();

        final CheckKvServersHealthWorker w
                = new CheckKvServersHealthWorker(100,

                replicationFactor, replicationThresholdSatisfied,

                kvServerClientsByContactPoint, kvServerHealthStateStatsByContactPoint, kvServerClientsByServerHealthStateStats);


        // when
        w.start();



        // then
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    assertTrue(replicationThresholdSatisfied.get());
                });



        // when (also another server goes down, so 2 are down out of 3, replication factor is 2, so it should NOT be satisfied)
        w.pauseCheck();
        while (!w.isPaused());


        Mockito.reset(kvServerClient2);
        Mockito.when(kvServerClient2.sendMessage(Operations.HEALTH_CHECK.getMsgOp()))
                .thenThrow(IOException.class);
        Mockito.when(kvServerClient2.makeCopy()).thenReturn(kvServerClient2);

        w.resumeCheck();


        // then
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    assertFalse(replicationThresholdSatisfied.get());
                });


        // when
        Mockito.reset(kvServerClient2);
        Mockito.when(kvServerClient2.sendMessage(Operations.HEALTH_CHECK.getMsgOp()))
                .thenReturn(ProtocolConstants.UNKNOWN_COMMAND_RESP);
        Mockito.when(kvServerClient2.makeCopy()).thenReturn(kvServerClient2);


        // then
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    assertFalse(replicationThresholdSatisfied.get());
                });


        // when
        Mockito.reset(kvServerClient2);
        Mockito.when(kvServerClient2.sendMessage(Operations.HEALTH_CHECK.getMsgOp()))
                .thenReturn(ProtocolConstants.OKAY_RESP);


        // then
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    assertTrue(replicationThresholdSatisfied.get());
                });


        // when
        Mockito.reset(kvServerClient3);
        Mockito.when(kvServerClient3.sendMessage(Operations.HEALTH_CHECK.getMsgOp()))
                .thenReturn(ProtocolConstants.OKAY_RESP);


        // then
        Awaitility.await().atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {

                    assertTrue(replicationThresholdSatisfied.get());
                });

    }

}