package chriniko.kv.broker.operation;

import chriniko.kv.broker.api.ConsistencyLevel;
import chriniko.kv.broker.api.KvServerContactPoint;
import chriniko.kv.broker.error.availability.*;
import chriniko.kv.broker.error.response.ErrorReceivedFromKvServerException;
import chriniko.kv.broker.health.KvServerHealthState;
import chriniko.kv.broker.infra.KvServerClient;
import chriniko.kv.broker.infra.SetUtils;
import chriniko.kv.datatypes.Value;
import chriniko.kv.protocol.Operations;
import chriniko.kv.protocol.ProtocolConstants;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KvBrokerPutOperation {


    public void process(String key, Value<?> value, ConsistencyLevel consistencyLevel,

                        ConcurrentHashMap<KvServerHealthState, Set<Pair<KvServerClient, KvServerContactPoint>>> kvServerClientsByServerHealthStateStats,
                        ConcurrentHashMap<KvServerContactPoint, KvServerClient> kvServerClientsByContactPoint,

                        int replicationFactor,
                        boolean replicationThresholdSatisfied) throws KvServerAvailabilityException, IOException, ErrorReceivedFromKvServerException {

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
            final int quorum = ConsistencyLevel.calculateQuorum(replicationFactor);

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
            if (!replicationThresholdSatisfied) {
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


    public void processRaw(String key, String value, ConsistencyLevel consistencyLevel,

                           ConcurrentHashMap<KvServerHealthState, Set<Pair<KvServerClient, KvServerContactPoint>>> kvServerClientsByServerHealthStateStats,
                           ConcurrentHashMap<KvServerContactPoint, KvServerClient> kvServerClientsByContactPoint,

                           int replicationFactor,
                           boolean replicationThresholdSatisfied) throws KvServerAvailabilityException, IOException, ErrorReceivedFromKvServerException {

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
            final int quorum = ConsistencyLevel.calculateQuorum(replicationFactor);

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
            if (!replicationThresholdSatisfied) {
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


    private void putOperation(KvServerClient kvServerClient, String key, Value<?> value) throws IOException, ErrorReceivedFromKvServerException {
        String serializedValue = value.asString();
        putOperation(kvServerClient, key, serializedValue);
    }

    public void putOperation(KvServerClient kvServerClient, String key, String value) throws IOException, ErrorReceivedFromKvServerException {
        System.out.println("will put key: " + key + " --- value: " + value);

        final String response = kvServerClient.sendMessage(Operations.PUT.getMsgOp() + " " + key + ": " + value);

        if (!ProtocolConstants.OKAY_RESP.equals(response)) {
            throw new ErrorReceivedFromKvServerException("error response received from kv-server: " + response, response);
        }
    }

}
