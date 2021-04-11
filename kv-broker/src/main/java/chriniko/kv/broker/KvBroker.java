package chriniko.kv.broker;

import chriniko.kv.broker.error.KvInfraBadStateException;
import chriniko.kv.broker.error.UncheckedKvInfraBadStateException;
import chriniko.kv.broker.error.availability.*;
import chriniko.kv.broker.error.response.ErrorReceivedFromKvServerException;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
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
public class KvBroker {

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


    public KvBroker() {
        kvServerClientsByContactPoint = new ConcurrentHashMap<>();

        kvServerHealthStateStatsByContactPoint = new ConcurrentHashMap<>();
        kvServerClientsByServerHealthStateStats = new ConcurrentHashMap<>();

        replicationThresholdSatisfied = new AtomicBoolean();
    }


    public void stop() {

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


    public void start(List<KvServerContactPoint> kvServerContactPoints,
                      BufferedReader dataToIndexBufferedReader,
                      boolean injectGeneratedData,
                      int replicationFactor,
                      Runnable readyAction) throws NotOkayResponseException, IOException, ErrorReceivedFromKvServerException {

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

    public boolean getReplicationThresholdSatisfied() {
        boolean r = replicationThresholdSatisfied.get();
        if (!r) {
            System.out.println("WARNING!!! k(replicationFactor) or more servers are down and therefore no guarantees for correct output can be satisfied");
        }
        return r;
    }

    public void put(String key, Value<?> value, ConsistencyLevel consistencyLevel) throws KvServerAvailabilityException, IOException, ErrorReceivedFromKvServerException {

        if (consistencyLevel == ConsistencyLevel.ONE) {

            // check servers state
            final Set<Pair<KvServerClient, KvServerContactPoint>> r = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);
            if (r == null || r.isEmpty()) {
                throw new NotAtLeastOneKvServerUpException("not at least one kv-server is up and running");
            }


            // pick correct ones and process
            final Pair<KvServerClient, KvServerContactPoint> selection = SetUtils.pickOneRandomly(r);

            System.out.println("will execute put operation against kv-server: " + selection.getValue1());

            final KvServerClient client = selection.getValue0();
            putOperation(client, key, value);

        } else if (consistencyLevel == ConsistencyLevel.ALL) {

            // check servers state
            final Set<Pair<KvServerClient, KvServerContactPoint>> r = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);
            if (r == null || r.isEmpty()) {
                throw new NotAtLeastOneKvServerUpException("not at least one kv-server is up and running");
            }
            if (r.size() < kvServerClientsByContactPoint.size()) {
                throw new NotAllKvServersAreUpException("only: " + r.size() + " servers are up, which you have registered: " + kvServerClientsByContactPoint.size() + " in total");
            }


            // pick correct ones and process
            for (Pair<KvServerClient, KvServerContactPoint> selection : r) {

                System.out.println("will execute put operation against kv-server: " + selection.getValue1());

                KvServerClient client = selection.getValue0();
                putOperation(client, key, value);
            }

        } else if (consistencyLevel == ConsistencyLevel.QUORUM) {

            // check servers state
            final int quorum = ConsistencyLevel.calculateQuorum(this.replicationFactor);

            final Set<Pair<KvServerClient, KvServerContactPoint>> r = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);
            if (r == null || r.isEmpty()) {
                throw new NotAtLeastOneKvServerUpException("not at least one kv-server is up and running");
            }
            if (r.size() < quorum) {
                throw new QuorumNotApplicableException("quorum: " + quorum + " for replicationFactor: " + replicationFactor + " is not applicable, due to limited up servers: " + r.size());
            }


            // pick correct ones and process
            final Set<Pair<KvServerClient, KvServerContactPoint>> selected = SetUtils.pickN(quorum, r);
            for (Pair<KvServerClient, KvServerContactPoint> s : selected) {
                System.out.println("will execute put operation against kv-server: " + s.getValue1());

                KvServerClient client = s.getValue0();
                putOperation(client, key, value);
            }

        } else if (consistencyLevel == ConsistencyLevel.REPLICATION_FACTOR) {

            // check servers state
            final Set<Pair<KvServerClient, KvServerContactPoint>> r = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);
            if (!this.getReplicationThresholdSatisfied()) {
                throw new ReplicationFactorNotApplicableException("replication factor: " + replicationFactor + " is not satisfied");
            }


            // pick correct ones and process
            final Set<Pair<KvServerClient, KvServerContactPoint>> selected = SetUtils.pickN(replicationFactor, r);
            for (Pair<KvServerClient, KvServerContactPoint> s : selected) {
                System.out.println("will execute put operation against kv-server: " + s.getValue1());

                KvServerClient client = s.getValue0();
                putOperation(client, key, value);
            }

        } else {
            throw new IllegalStateException("consistency level provided not supported!");
        }
    }


    public Optional<Value<?>> get(String key, ConsistencyLevel consistencyLevel) throws NotAtLeastOneKvServerUpException, IOException, ErrorReceivedFromKvServerException, NotAllKvServersAreUpException, QuorumNotApplicableException, ReplicationFactorNotApplicableException {


        if (consistencyLevel == ConsistencyLevel.ONE) {

            // check servers state
            final Set<Pair<KvServerClient, KvServerContactPoint>> r = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);
            if (r == null || r.isEmpty()) {
                throw new NotAtLeastOneKvServerUpException("not at least one kv-server is up and running");
            }


            // pick correct ones and process
            final Pair<KvServerClient, KvServerContactPoint> selection = SetUtils.pickOneRandomly(r);

            System.out.println("will execute get operation against kv-server: " + selection.getValue1());

            final KvServerClient client = selection.getValue0();
            return getOperation(client, key);

        } else if (consistencyLevel == ConsistencyLevel.ALL) {

            // check servers state
            final Set<Pair<KvServerClient, KvServerContactPoint>> r = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);
            if (r == null || r.isEmpty()) {
                throw new NotAtLeastOneKvServerUpException("not at least one kv-server is up and running");
            }
            if (r.size() < kvServerClientsByContactPoint.size()) {
                throw new NotAllKvServersAreUpException("only: " + r.size() + " servers are up, which you have registered: " + kvServerClientsByContactPoint.size() + " in total");
            }


            // pick correct ones and process
            // todo SEARCH-GET STRATEGY :: firstFoundFirstReturned or find all and merge based on create timestamp and return the most fresh ???
            boolean firstFoundFirstReturned = true;

            boolean foundResult = false;
            Value<?> result = null;

            for (Pair<KvServerClient, KvServerContactPoint> selection : r) {

                final KvServerClient client = selection.getValue0();
                Optional<Value<?>> searchResult = getOperation(client, key);

                if (firstFoundFirstReturned) {

                    if (searchResult.isPresent()) {
                        result = searchResult.get();
                        foundResult = true;
                        break;
                    }

                } else {
                    throw new UnsupportedOperationException("TODO::SEARCH-GET STRATEGY");
                }

            }

            return !foundResult ? Optional.empty() : Optional.of(result);

        } else if (consistencyLevel == ConsistencyLevel.QUORUM) {

            // check servers state
            final int quorum = ConsistencyLevel.calculateQuorum(this.replicationFactor);

            final Set<Pair<KvServerClient, KvServerContactPoint>> r = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);
            if (r == null || r.isEmpty()) {
                throw new NotAtLeastOneKvServerUpException("not at least one kv-server is up and running");
            }
            if (r.size() < quorum) {
                throw new QuorumNotApplicableException("quorum: " + quorum + " for replicationFactor: " + replicationFactor + " is not applicable, due to limited up servers: " + r.size());
            }


            // pick correct ones and process
            // todo SEARCH-GET STRATEGY :: firstFoundFirstReturned or find all and merge based on create timestamp and return the most fresh ???
            boolean firstFoundFirstReturned = true;

            boolean foundResult = false;
            Value<?> result = null;

            for (Pair<KvServerClient, KvServerContactPoint> selection : r) {

                final KvServerClient client = selection.getValue0();
                Optional<Value<?>> searchResult = getOperation(client, key);

                if (firstFoundFirstReturned) {

                    if (searchResult.isPresent()) {
                        result = searchResult.get();
                        foundResult = true;
                        break;
                    }

                } else {
                    throw new UnsupportedOperationException("TODO::SEARCH-GET STRATEGY");
                }

            }

            return !foundResult ? Optional.empty() : Optional.of(result);

        } else if (consistencyLevel == ConsistencyLevel.REPLICATION_FACTOR) {


            // check servers state
            final Set<Pair<KvServerClient, KvServerContactPoint>> r = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);
            if (!this.getReplicationThresholdSatisfied()) {
                throw new ReplicationFactorNotApplicableException("replication factor: " + replicationFactor + " is not satisfied");
            }


            // pick correct ones and process
            // todo SEARCH-GET STRATEGY :: firstFoundFirstReturned or find all and merge based on create timestamp and return the most fresh ???
            boolean firstFoundFirstReturned = true;

            boolean foundResult = false;
            Value<?> result = null;

            for (Pair<KvServerClient, KvServerContactPoint> selection : r) {

                final KvServerClient client = selection.getValue0();
                Optional<Value<?>> searchResult = getOperation(client, key);

                if (firstFoundFirstReturned) {

                    if (searchResult.isPresent()) {
                        result = searchResult.get();
                        foundResult = true;
                        break;
                    }

                } else {
                    throw new UnsupportedOperationException("TODO::SEARCH-GET STRATEGY");
                }

            }

            return !foundResult ? Optional.empty() : Optional.of(result);

        } else {
            throw new IllegalStateException("consistency level provided not supported!");
        }
    }


    public boolean delete(String key, ConsistencyLevel consistencyLevel) {
        //TODO
        throw new UnsupportedOperationException("TODO");
    }


    // todo query


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
                this.putOperation(kvServerClient, key, value);
            }
        }
    }

    private void putOperation(KvServerClient kvServerClient, String key, Value<?> value) throws IOException, ErrorReceivedFromKvServerException {
        String serializedValue = value.asString();
        putOperation(kvServerClient, key, serializedValue);
    }

    private void putOperation(KvServerClient kvServerClient, String key, String value) throws IOException, ErrorReceivedFromKvServerException {
        System.out.println("will put key: " + key + " --- value: " + value);

        final String response = kvServerClient.sendMessage(Operations.PUT.getMsgOp() + " " + key + ": " + value);

        if (!ProtocolConstants.OKAY_RESP.equals(response)) {
            throw new ErrorReceivedFromKvServerException("error response received from kv-server: " + response, response);
        }
    }


    private Optional<Value<?>> getOperation(KvServerClient kvServerClient, String key) throws IOException, ErrorReceivedFromKvServerException {
        System.out.println("will get value for key: " + key);

        final String response = kvServerClient.sendMessage(Operations.GET.getMsgOp() + " " + key);

        if (response.contains(ProtocolConstants.OKAY_RESP)) {

            String prefix = ProtocolConstants.OKAY_RESP + "#";
            String r = response.substring(prefix.length());

            try {
                Value<?> v = DatatypesAntlrParser.process(r);
                return Optional.of(v);
            } catch (ParsingException e) {
                KvInfraBadStateException error = new KvInfraBadStateException("kv-server db in bad state:: could not parse the response received, which should be valid, most probably malformed data entered to kv-server");
                throw new UncheckedKvInfraBadStateException(error);
            }

        } else if (ProtocolConstants.NOT_FOUND_RESP.equals(response)) {
            return Optional.empty();
        } else {
            throw new ErrorReceivedFromKvServerException("error response received from kv-server: " + response, response);
        }

    }

}
