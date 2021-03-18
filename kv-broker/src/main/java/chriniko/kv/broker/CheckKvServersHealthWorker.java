package chriniko.kv.broker;

import chriniko.kv.protocol.Operations;
import chriniko.kv.protocol.ProtocolConstants;
import org.javatuples.Pair;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The purpose of this worker-thread is to hit at a fixed time interval (consistency gap) all the
 * servers connected to the broker and extract their health state, and group them accordingly (UP, DOWN, NOT_OKAY).
 *
 *
 * Moreover it checks if replicationFactor policy is satisfied, for example if replicationFactor(k) == 3 and we have
 * 5 servers running, but 2 of them are in DOWN or in NOT_OKAY state then replicationFactor is satisfied.
 *
 * If replicationFactor(k) == 4 and we have
 * 5 servers running, but 3 of them are in DOWN or in NOT_OKAY state then replicationFactor is NOT satisfied (because only 2 are UP,
 * so we will replicate to 2 instead of 4).
 *
 *
 */
public class CheckKvServersHealthWorker extends Thread {

    private final long pacingInMs;

    private final int replicationFactor;
    private final AtomicBoolean replicationThresholdSatisfied;


    /**
     * From this we calculate the: {@link CheckKvServersHealthWorker#kvServerHealthStateStatsByContactPoint} and
     * {@link CheckKvServersHealthWorker#kvServerClientsByServerHealthStateStats}
     */
    private final ConcurrentHashMap<KvServerContactPoint, KvServerClient> kvServerClientsByContactPoint;


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

        this.kvServerClientsByContactPoint = kvServerClientsByContactPoint;

        this.kvServerHealthStateStatsByContactPoint = kvServerHealthStateStatsByContactPoint;
        this.kvServerClientsByServerHealthStateStats = kvServerClientsByServerHealthStateStats;
    }


    @Override
    public void run() {

        for (; ; ) {

            // Note: consistency gap for checking health for all servers
            try {
                System.out.println("pacing for checkServersHealthWorker...");
                Thread.sleep(pacingInMs);
            } catch (InterruptedException e) {
                if (!Thread.currentThread().isInterrupted()) {
                    Thread.currentThread().interrupt();
                }
            }

            if (Thread.currentThread().isInterrupted()) {
                System.out.println("checkServersHealthWorker thread killed!");
                break;
            }

            kvServerClientsByServerHealthStateStats.clear();

            // Note: time to maintain the state of each server connected to broker (is up? is down? is not_okay? etc...)
            kvServerClientsByContactPoint.forEach((kvServerContactPoint, kvServerClient) -> {

                System.out.println("will check health check for server at: " + kvServerContactPoint);

                final KvServerHealthState kvServerHealthState;
                try {
                    final String response = kvServerClient.sendMessage(Operations.HEALTH_CHECK.getMsgOp());

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
                    kvServerClientsByServerHealthStateStats.compute(kvServerHealthState, (k, v) -> {
                        if (v == null) {
                            v = ConcurrentHashMap.newKeySet();
                        }
                        v.add(Pair.with(kvServerClient, kvServerContactPoint));
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
                    kvServerClientsByServerHealthStateStats.compute(KvServerHealthState.DOWN, (k, v) -> {
                        if (v == null) {
                            v = ConcurrentHashMap.newKeySet();
                        }
                        v.add(Pair.with(kvServerClient, kvServerContactPoint));

                        return v;
                    });
                    // TODO in case it is down we should replace also the KvServerClient in kvServerClientsByContactPoint

                }
            });


            // Note: time to check if replication threshold is satisfied.
            final Set<Pair<KvServerClient, KvServerContactPoint>> serversWithAppStatus
                    = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);

            if (serversWithAppStatus.isEmpty()) {
                System.out.println("not at least one server is up....infra is critical");

            } else {
                System.out.println("servers with up status: " + serversWithAppStatus);

                replicationThresholdSatisfied.set(
                        serversWithAppStatus.size() >= replicationFactor
                );
            }
        }

    }
}
