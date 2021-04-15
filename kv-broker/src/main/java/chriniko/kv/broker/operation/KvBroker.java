package chriniko.kv.broker.operation;

import chriniko.kv.broker.api.ConsistencyLevel;
import chriniko.kv.broker.api.KvBrokerApi;
import chriniko.kv.broker.api.KvServerContactPoint;
import chriniko.kv.broker.api.QueryKey;
import chriniko.kv.broker.error.KvBrokerInvalidCallException;
import chriniko.kv.broker.error.availability.*;
import chriniko.kv.broker.error.response.ErrorReceivedFromKvServerException;
import chriniko.kv.broker.health.CheckKvServersHealthWorker;
import chriniko.kv.broker.health.KvServerHealthState;
import chriniko.kv.broker.health.KvServerHealthStateStats;
import chriniko.kv.broker.infra.KvServerClient;
import chriniko.kv.datatypes.Value;
import chriniko.kv.protocol.NotOkayResponseException;
import chriniko.kv.protocol.Operations;
import chriniko.kv.protocol.ProtocolConstants;
import org.javatuples.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * The broker should start with the following command:
 * kvBroker -s serverFile.txt -i dataToIndex.txt -k 2
 * <p>
 * The  serverFile.txt is a space separated list of server IPs and their respective ports that will be
 * listening for queries and indexing commands. For example:
 * 123.123.12.12 8000
 * 123.123.12.12 8001
 * 123.2.3.4 9000
 * <p>
 * Is an example of a serverfile indicating that this broker will be working with 3 servers with the IPs
 * described and on the respective ports described.
 * <p>
 * The dataToIndex.txt is a file containing data that was output from the previous part of the project (check data-injector) that was generating the data.
 * <p>
 * The k value is the replication factor, i.e. how many different servers will have the same replicated data.
 */
public class KvBroker implements KvBrokerApi {

    private static final int CHECK_SERVERS_HEALTH_WORKER_PACING_IN_MS = 1000;

    /**
     * Server client instance per server contact point.
     */
    private final ConcurrentHashMap<KvServerContactPoint, KvServerClient> kvServerClientsByContactPoint;


    /**
     * This is populated from {@link CheckKvServersHealthWorker} with the help of {@link KvBroker#kvServerClientsByContactPoint}
     */
    private final ConcurrentHashMap<KvServerContactPoint, KvServerHealthStateStats> kvServerHealthStateStatsByContactPoint;


    /**
     * This is populated from {@link CheckKvServersHealthWorker} with the help of {@link KvBroker#kvServerClientsByContactPoint}
     */
    private final ConcurrentHashMap<KvServerHealthState, Set<Pair<KvServerClient, KvServerContactPoint>>> kvServerClientsByServerHealthStateStats;


    /**
     * Since we implemented k-replication, the broker should continue to work unless not at least k available servers (up and healthy).
     * For example, for k=2 if we had 3 servers running and one is down (i.e 2 left) the server can still compute correct results.
     * If >=2 servers are down the broker should output a warning indicating that k or more servers are down and therefore it cannot
     * guarantee the correct output.
     */
    private int replicationFactor;


    private final AtomicBoolean replicationThresholdSatisfied;
    private CheckKvServersHealthWorker checkKvServersHealthWorker;

    private boolean started = false;


    // ops
    private final KvBrokerDeleteOperation kvBrokerDeleteOperation = new KvBrokerDeleteOperation();
    private final KvBrokerGetOperation kvBrokerGetOperation = new KvBrokerGetOperation();
    private final KvBrokerQueryOperation kvBrokerQueryOperation = new KvBrokerQueryOperation();
    private final KvBrokerPutOperation kvBrokerPutOperation = new KvBrokerPutOperation();


    public KvBroker() {
        kvServerClientsByContactPoint = new ConcurrentHashMap<>();

        kvServerHealthStateStatsByContactPoint = new ConcurrentHashMap<>();
        kvServerClientsByServerHealthStateStats = new ConcurrentHashMap<>();

        replicationThresholdSatisfied = new AtomicBoolean();
    }

    @Override
    public void stop() {
        if (!started) {
            throw new KvBrokerInvalidCallException("called stop on not started broker");
        }

        if (!kvServerClientsByContactPoint.isEmpty()) {

            kvServerClientsByContactPoint.forEach((point, client) -> {

                try {
                    System.out.println("will stop kv-server-client for: " + point);
                    client.stop();
                } catch (IOException ignored) {
                }

            });
        }

        if (checkKvServersHealthWorker != null) {
            checkKvServersHealthWorker.interrupt();
        }

    }

    @Override
    public void start(List<KvServerContactPoint> kvServerContactPoints,
                      BufferedReader dataToIndexBufferedReader,
                      boolean injectGeneratedData,
                      int replicationFactor,
                      Runnable readyAction) throws NotOkayResponseException, IOException, ErrorReceivedFromKvServerException {

        if (started) {
            throw new KvBrokerInvalidCallException("called start on already started broker");
        }
        started = true;

        if (kvServerContactPoints.size() < replicationFactor) {
            throw new IllegalArgumentException("provided kv-server contact points(-s) are less than provided replicationFactor(-k)");
        }

        this.replicationFactor = replicationFactor;

        // Note: first try to connect to all provided contact points
        connectToContactPoints(kvServerContactPoints);

        // Note: do our housekeeping stuff when kv broker gets terminated.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("will close all kvServerClients now");

            kvServerClientsByContactPoint.forEach(((kvServerContactPoint, kvServerClient) -> {
                try {
                    kvServerClient.stop();
                } catch (IOException ignored) {
                }
            }));

        }));


        // Note: now that we have connected to all kv-servers, time to start healthCheck thread-worker, which at a fixed time interval will
        //       check for the availability (is up?) for all the kvServerContactPoints and keep track of the status on a map
        checkKvServersHealthWorker = new CheckKvServersHealthWorker(CHECK_SERVERS_HEALTH_WORKER_PACING_IN_MS,

                replicationFactor, replicationThresholdSatisfied,

                kvServerClientsByContactPoint,
                kvServerHealthStateStatsByContactPoint, kvServerClientsByServerHealthStateStats
        );
        checkKvServersHealthWorker.setName("checkServersHealthWorker");
        checkKvServersHealthWorker.setDaemon(false);
        checkKvServersHealthWorker.start();


        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("will kill checkServersHealthWorker now...");
            checkKvServersHealthWorker.interrupt();
        }));


        // Note: now is time for each line of generated data to randomly pick k(replicationFactor) servers and send a request of the form PUT data.
        if (injectGeneratedData) {
            sendGeneratedData(dataToIndexBufferedReader, replicationFactor);
        }

        // Note: give some time to checkKvServersHealthWorker to run.
        int totalTries = 5;
        int pacingInMs = 150;
        int currentTry = 0;
        while (currentTry++ < totalTries) {

            final Set<Pair<KvServerClient, KvServerContactPoint>> r = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);
            if (r != null && r.size() == kvServerClientsByContactPoint.size()) {
                break;
            }

            try {
                Thread.sleep(pacingInMs);
            } catch (InterruptedException ignored) {
            }
        }

        if (readyAction != null) {
            readyAction.run();
        }
        System.out.println("start method finished...");
    }

    @Override
    public boolean getReplicationThresholdSatisfied() {
        boolean r = replicationThresholdSatisfied.get();
        if (!r) {
            System.out.println("WARNING!!! k(replicationFactor) or more servers are down and therefore no guarantees for correct output can be satisfied");
        }
        return r;
    }

    @Override
    public void put(String key, Value<?> value, ConsistencyLevel consistencyLevel) throws KvServerAvailabilityException, IOException, ErrorReceivedFromKvServerException {

        kvBrokerPutOperation.process(key, value, consistencyLevel,

                kvServerClientsByServerHealthStateStats, kvServerClientsByContactPoint,

                replicationFactor, getReplicationThresholdSatisfied()
        );

    }

    @Override
    public void putRaw(String key, String value, ConsistencyLevel consistencyLevel) throws KvServerAvailabilityException, IOException, ErrorReceivedFromKvServerException {

        kvBrokerPutOperation.processRaw(key, value, consistencyLevel,

                kvServerClientsByServerHealthStateStats, kvServerClientsByContactPoint,

                replicationFactor, getReplicationThresholdSatisfied()
        );
    }

    @Override
    public Optional<Value<?>> get(String key, ConsistencyLevel consistencyLevel) throws NotAtLeastOneKvServerUpException, IOException, ErrorReceivedFromKvServerException, NotAllKvServersAreUpException, QuorumNotApplicableException, ReplicationFactorNotApplicableException {

        return kvBrokerGetOperation.process(key, consistencyLevel,

                kvServerClientsByServerHealthStateStats, kvServerClientsByContactPoint,

                replicationFactor, getReplicationThresholdSatisfied()
        );
    }


    /**
     * If delete was successful it returns the deleted record.
     *
     * @param key
     * @param consistencyLevel
     * @return returns the deleted record if delete was successful (key was an existing one)
     */
    @Override
    public Optional<Value<?>> delete(String key, ConsistencyLevel consistencyLevel) throws IOException, ErrorReceivedFromKvServerException, NotAtLeastOneKvServerUpException, NotAllKvServersAreUpException, ReplicationFactorNotApplicableException, QuorumNotApplicableException {

        return kvBrokerDeleteOperation.process(key, consistencyLevel,

                kvServerClientsByServerHealthStateStats, kvServerClientsByContactPoint,

                replicationFactor, getReplicationThresholdSatisfied()
        );
    }


    @Override
    public Optional<Value<?>> query(QueryKey key, ConsistencyLevel consistencyLevel) throws NotAtLeastOneKvServerUpException {

        return kvBrokerQueryOperation.process(key, consistencyLevel,

                kvServerClientsByServerHealthStateStats, kvServerClientsByContactPoint,

                replicationFactor, getReplicationThresholdSatisfied()
        );

    }


    @Override
    public Map<KvServerContactPoint, KvServerClient> getKvServerClientsByContactPoint() {
        return Collections.unmodifiableMap(kvServerClientsByContactPoint);
    }


    // --- infra ---

    private void connectToContactPoints(List<KvServerContactPoint> kvServerContactPoints) throws NotOkayResponseException, IOException {

        for (KvServerContactPoint kvServerContactPoint : kvServerContactPoints) {
            try {
                KvServerClient kvServerClient = KvServerClient.start(kvServerContactPoint.getHost(), kvServerContactPoint.getPort());

                String response = kvServerClient.sendMessage(Operations.HEALTH_CHECK.getMsgOp());
                System.out.println("response received from kv-server: " + kvServerContactPoint + ", response: " + response);

                if (ProtocolConstants.OKAY_RESP.equals(response)) {

                    System.out.println("connected successfully to kv-server: " + kvServerContactPoint);
                    kvServerClientsByContactPoint.put(kvServerContactPoint, kvServerClient);

                } else {
                    System.err.println("not received okay response from kv-server: " + kvServerContactPoint);

                    String msg = "not received okay response from kv-server: " + kvServerContactPoint + ", response: " + response;
                    throw new NotOkayResponseException(msg);
                }

            } catch (IOException e) {
                System.err.println("could not connect to kv-server: " + kvServerContactPoint + " because it was during of the bootstrap of the kv-broker it will fail");
                throw e;
            }
        }
    }

    private void sendGeneratedData(BufferedReader dataToIndexBufferedReader, int replicationFactor) throws IOException, ErrorReceivedFromKvServerException {
        if (dataToIndexBufferedReader == null) {
            InputStream in = this.getClass().getResourceAsStream("/sampleDataToIndex.txt");
            dataToIndexBufferedReader = new BufferedReader(new InputStreamReader(in));
        }

        final Set<KvServerContactPoint> contactPointsToSendRequests = new HashSet<>();
        final ArrayList<KvServerContactPoint> contactPoints = new ArrayList<>(kvServerClientsByContactPoint.keySet());
        // randomly pick k contact points to send the data.
        while (contactPointsToSendRequests.size() < replicationFactor) {
            int randomIdx = ThreadLocalRandom.current().nextInt(contactPoints.size());

            KvServerContactPoint cPoint = contactPoints.get(randomIdx);
            contactPointsToSendRequests.add(cPoint);
        }

        System.out.println("randomly picked k (replicationFactor) contact points: " + contactPointsToSendRequests + " to send generated data");
        String line;
        while ((line = dataToIndexBufferedReader.readLine()) != null) {

            System.out.println("will send line: " + line);

            for (KvServerContactPoint contactPointsToSendRequest : contactPointsToSendRequests) {
                String key = line.split(":")[0];
                String value = line.substring(key.length() + 1 /* Note: plus one in order to not have the : */);

                KvServerClient kvServerClient = kvServerClientsByContactPoint.get(contactPointsToSendRequest);
                kvBrokerPutOperation.putOperation(kvServerClient, key, value);
            }
        }
    }

}
