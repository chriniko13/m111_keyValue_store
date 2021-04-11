package chriniko.kv.broker;

import chriniko.kv.protocol.Operations;
import chriniko.kv.protocol.ProtocolConstants;
import lombok.Getter;
import org.javatuples.Pair;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The purpose of this worker-thread is to hit at a fixed time interval (consistency gap) all the
 * servers connected to the broker and extract their health state, and group them accordingly (UP, DOWN, NOT_OKAY).
 * <p>
 * <p>
 * Moreover it checks if replicationFactor policy is satisfied, for example if replicationFactor(k) == 3 and we have
 * 5 servers running, but 2 of them are in DOWN or in NOT_OKAY state then replicationFactor is satisfied.
 * <p>
 * If replicationFactor(k) == 4 and we have
 * 5 servers running, but 3 of them are in DOWN or in NOT_OKAY state then replicationFactor is NOT satisfied (because only 2 are UP,
 * so we will replicate to 2 instead of 4).
 */
public class CheckKvServersHealthWorker extends Thread {

    private final long pacingInMs;

    private final int replicationFactor;
    private final AtomicBoolean replicationThresholdSatisfied;

    private final Object workMutex = new Object();
    private boolean work;

    @Getter
    private volatile boolean paused;

    /**
     * From this we calculate the: {@link CheckKvServersHealthWorker#kvServerHealthStateStatsByContactPoint} and
     * {@link CheckKvServersHealthWorker#kvServerClientsByServerHealthStateStats}
     */
    private final ConcurrentHashMap<
            KvServerContactPoint,
            Pair<KvServerClient /*owned by broker*/, KvServerClient /*owned by health worker*/>
            > kvServerClientsByContactPoint;


    private final ConcurrentHashMap<KvServerContactPoint, KvServerHealthStateStats> kvServerHealthStateStatsByContactPoint;
    private final ConcurrentHashMap<KvServerHealthState, Set<Pair<KvServerClient, KvServerContactPoint>>> kvServerClientsByServerHealthStateStats;

    public CheckKvServersHealthWorker(long pacingInMs,

                                      int replicationFactor,
                                      AtomicBoolean replicationThresholdSatisfied,

                                      ConcurrentHashMap<KvServerContactPoint, KvServerClient> kvServerClientsByContactPoint,

                                      ConcurrentHashMap<KvServerContactPoint, KvServerHealthStateStats> kvServerHealthStateStatsByContactPoint,
                                      ConcurrentHashMap<KvServerHealthState, Set<Pair<KvServerClient, KvServerContactPoint>>> kvServerClientsByServerHealthStateStats) {

        this.pacingInMs = pacingInMs;

        this.replicationFactor = replicationFactor;
        this.replicationThresholdSatisfied = replicationThresholdSatisfied;

        this.work = true;


        this.kvServerClientsByContactPoint = new ConcurrentHashMap<>();
        kvServerClientsByContactPoint.forEach((kvServerContactPoint, kvServerClient) -> {

            try {
                KvServerClient toUseForHealthCheck = kvServerClient.makeCopy();

                System.out.println("ownedByBroker: " + kvServerClient + " --- toUseForHealthCheck: " + toUseForHealthCheck);

                this.kvServerClientsByContactPoint.put(kvServerContactPoint, Pair.with(kvServerClient, toUseForHealthCheck));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

        });


        this.kvServerHealthStateStatsByContactPoint = kvServerHealthStateStatsByContactPoint;
        this.kvServerClientsByServerHealthStateStats = kvServerClientsByServerHealthStateStats;
    }

    public void resumeCheck() {
        synchronized (workMutex) {
            work = true;
        }
    }

    public void pauseCheck() {
        synchronized (workMutex) {
            work = false;
        }
    }

    @Override
    public void run() {

        for (; ; ) {

            // implement resume/pause check functionality
            synchronized (workMutex) {
                if (!work) {
                    System.out.println("checkKvServersHealthWorker has been put to sleep....");
                    paused = true;
                    continue;
                } else {
                    System.out.println("checkKvServersHealthWorker has woke up....");
                    paused = false;
                }
            }



            if (Thread.currentThread().isInterrupted()) {
                System.out.println("checkServersHealthWorker thread killed!");
                break;
            }


            // Note: time to maintain the state of each server connected to broker (is up? is down? is not_okay? etc...)

            for (Map.Entry<KvServerContactPoint, Pair<KvServerClient, KvServerClient>> entry : kvServerClientsByContactPoint.entrySet()) {

                final KvServerContactPoint kvServerContactPoint = entry.getKey();
                final Pair<KvServerClient, KvServerClient> p = entry.getValue();

                final KvServerClient kvServerClientToMove = p.getValue0();
                final KvServerClient kvServerClientToCheckHealth = p.getValue1();


                System.out.println("will check health check for server at: " + kvServerContactPoint);

                final KvServerHealthState kvServerHealthState;
                try {
                    final String response = kvServerClientToCheckHealth.sendMessage(Operations.HEALTH_CHECK.getMsgOp());

                    if (ProtocolConstants.OKAY_RESP.equals(response)) {
                        kvServerHealthState = KvServerHealthState.UP;
                    } else {
                        kvServerHealthState = KvServerHealthState.NOT_OKAY;
                    }

                    // ----
                    kvServerHealthStateStatsByContactPoint.compute(kvServerContactPoint, (k, v) -> {
                        if (v == null) {
                            v = new KvServerHealthStateStats();
                        }
                        v.setLastTimeChecked(Instant.now());
                        v.setHealthState(kvServerHealthState);
                        return v;
                    });


                    // ----
                    if (kvServerHealthState == KvServerHealthState.UP) {
                        // note: maintain valid state.

                        Set<Pair<KvServerClient, KvServerContactPoint>> t = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.DOWN);
                        if (t != null) {
                            t.remove(Pair.with(kvServerClientToMove, kvServerContactPoint));
                        }

                        t = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.NOT_OKAY);
                        if (t != null) {
                            t.remove(Pair.with(kvServerClientToMove, kvServerContactPoint));
                        }


                    } else if (kvServerHealthState == KvServerHealthState.NOT_OKAY) {
                        // note: maintain valid state.
                        Set<Pair<KvServerClient, KvServerContactPoint>> t = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.DOWN);
                        if (t != null) {
                            t.remove(Pair.with(kvServerClientToMove, kvServerContactPoint));
                        }

                        t = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);
                        if (t != null) {
                            t.remove(Pair.with(kvServerClientToMove, kvServerContactPoint));
                        }
                    }

                    kvServerClientsByServerHealthStateStats.compute(kvServerHealthState, (k, v) -> {
                        if (v == null) {
                            v = ConcurrentHashMap.newKeySet();
                        }
                        v.add(Pair.with(kvServerClientToMove, kvServerContactPoint));
                        return v;
                    });


                } catch (IOException e) {
                    System.err.println("could not check-connect to server: " + kvServerContactPoint + " for checking the health");


                    // ----
                    kvServerHealthStateStatsByContactPoint.compute(kvServerContactPoint, (k, v) -> {
                        if (v == null) {
                            v = new KvServerHealthStateStats();
                        }
                        v.setLastTimeChecked(Instant.now());
                        v.setHealthState(KvServerHealthState.DOWN);

                        return v;
                    });


                    // ----
                    // note: maintain valid state.
                    Set<Pair<KvServerClient, KvServerContactPoint>> t = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);
                    if (t != null) {
                        t.remove(Pair.with(kvServerClientToMove, kvServerContactPoint));
                    }

                    t = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.NOT_OKAY);
                    if (t != null) {
                        t.remove(Pair.with(kvServerClientToMove, kvServerContactPoint));
                    }

                    kvServerClientsByServerHealthStateStats.compute(KvServerHealthState.DOWN, (k, v) -> {
                        if (v == null) {
                            v = ConcurrentHashMap.newKeySet();
                        }
                        v.add(Pair.with(kvServerClientToMove, kvServerContactPoint));

                        return v;
                    });

                }

            }


            // Note: time to check if replication threshold is satisfied.
            final Set<Pair<KvServerClient, KvServerContactPoint>> serversWithAppStatus
                    = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);

            if (serversWithAppStatus == null || serversWithAppStatus.isEmpty()) {
                System.out.println("not at least one server is up....infra is critical");

            } else {
                System.out.println("servers with up status: " + serversWithAppStatus);

                replicationThresholdSatisfied.set(
                        serversWithAppStatus.size() >= replicationFactor
                );
            }


            // Note: consistency gap for checking health for all servers
            try {
                System.out.println("pacing for checkServersHealthWorker...");
                Thread.sleep(pacingInMs);
            } catch (InterruptedException e) {
                if (!Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }

        } // for.

    }
}
