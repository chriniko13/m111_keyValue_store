package chriniko.kv.broker.operation;

import chriniko.kv.broker.api.ConsistencyLevel;
import chriniko.kv.broker.api.KvServerContactPoint;
import chriniko.kv.broker.error.KvInfraBadStateException;
import chriniko.kv.broker.error.UncheckedKvInfraBadStateException;
import chriniko.kv.broker.error.availability.NotAllKvServersAreUpException;
import chriniko.kv.broker.error.availability.NotAtLeastOneKvServerUpException;
import chriniko.kv.broker.error.availability.QuorumNotApplicableException;
import chriniko.kv.broker.error.availability.ReplicationFactorNotApplicableException;
import chriniko.kv.broker.error.response.ErrorReceivedFromKvServerException;
import chriniko.kv.broker.health.KvServerHealthState;
import chriniko.kv.broker.infra.KvServerClient;
import chriniko.kv.broker.infra.SetUtils;
import chriniko.kv.datatypes.Value;
import chriniko.kv.datatypes.error.ParsingException;
import chriniko.kv.datatypes.parser.DatatypesAntlrParser;
import chriniko.kv.protocol.Operations;
import chriniko.kv.protocol.ProtocolConstants;
import org.javatuples.Pair;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KvBrokerDeleteOperation {



    public Optional<Value<?>> process(String key, ConsistencyLevel consistencyLevel,

                                     ConcurrentHashMap<KvServerHealthState, Set<Pair<KvServerClient, KvServerContactPoint>>> kvServerClientsByServerHealthStateStats,
                                     ConcurrentHashMap<KvServerContactPoint, KvServerClient> kvServerClientsByContactPoint,

                                     int replicationFactor,
                                     boolean replicationThresholdSatisfied

    ) throws IOException, ErrorReceivedFromKvServerException, NotAtLeastOneKvServerUpException,
            NotAllKvServersAreUpException, ReplicationFactorNotApplicableException, QuorumNotApplicableException {

        if (consistencyLevel == ConsistencyLevel.ONE) {

            // check servers state
            final Set<Pair<KvServerClient, KvServerContactPoint>> r = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);
            if (r == null || r.isEmpty()) {
                throw new NotAtLeastOneKvServerUpException("not at least one kv-server is up and running");
            }


            // pick correct ones and process
            final Pair<KvServerClient, KvServerContactPoint> selection = SetUtils.pickOneRandomly(r);

            System.out.println("will execute delete operation against kv-server: " + selection.getValue1());

            final KvServerClient client = selection.getValue0();
            return deleteOperation(client, key);


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
            final Set<Value<?>> resultsReturnedFromEachServer = new LinkedHashSet<>();
            for (Pair<KvServerClient, KvServerContactPoint> selection : r) {

                System.out.println("will execute delete operation against kv-server: " + selection.getValue1());

                final KvServerClient client = selection.getValue0();

                deleteOperation(client, key).ifPresent(deleteResult -> {
                    resultsReturnedFromEachServer.add(deleteResult);
                });
            }

            if (resultsReturnedFromEachServer.isEmpty()) {
                return Optional.empty();
            } else {

                if (resultsReturnedFromEachServer.size() == 1) {
                    return Optional.ofNullable(resultsReturnedFromEachServer.iterator().next());
                } else {
                    KvInfraBadStateException error = new KvInfraBadStateException("resultsReturnedFromEachServer.size() == 1 ---> false");
                    throw new UncheckedKvInfraBadStateException(error);
                }
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

            final Set<Value<?>> resultsReturnedFromEachServer = new LinkedHashSet<>();
            for (Pair<KvServerClient, KvServerContactPoint> selection : selected) {

                System.out.println("will execute delete operation against kv-server: " + selection.getValue1());

                final KvServerClient client = selection.getValue0();

                deleteOperation(client, key).ifPresent(deleteResult -> {
                    resultsReturnedFromEachServer.add(deleteResult);
                });
            }

            if (resultsReturnedFromEachServer.isEmpty()) {
                return Optional.empty();
            } else {

                if (resultsReturnedFromEachServer.size() == 1) {
                    return Optional.ofNullable(resultsReturnedFromEachServer.iterator().next());
                } else {
                    KvInfraBadStateException error = new KvInfraBadStateException("resultsReturnedFromEachServer.size() == 1 ---> false");
                    throw new UncheckedKvInfraBadStateException(error);
                }
            }


        } else if (consistencyLevel == ConsistencyLevel.REPLICATION_FACTOR) {

            // check servers state
            final Set<Pair<KvServerClient, KvServerContactPoint>> r = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);
            if (!replicationThresholdSatisfied) {
                throw new ReplicationFactorNotApplicableException("replication factor: " + replicationFactor + " is not satisfied");
            }

            // pick correct ones and process
            final Set<Pair<KvServerClient, KvServerContactPoint>> selected = SetUtils.pickN(replicationFactor, r);

            final Set<Value<?>> resultsReturnedFromEachServer = new LinkedHashSet<>();
            for (Pair<KvServerClient, KvServerContactPoint> selection : selected) {

                System.out.println("will execute delete operation against kv-server: " + selection.getValue1());

                final KvServerClient client = selection.getValue0();

                deleteOperation(client, key).ifPresent(deleteResult -> {
                    resultsReturnedFromEachServer.add(deleteResult);
                });
            }

            if (resultsReturnedFromEachServer.isEmpty()) {
                return Optional.empty();
            } else {

                if (resultsReturnedFromEachServer.size() == 1) {
                    return Optional.ofNullable(resultsReturnedFromEachServer.iterator().next());
                } else {
                    KvInfraBadStateException error = new KvInfraBadStateException("resultsReturnedFromEachServer.size() == 1 ---> false");
                    throw new UncheckedKvInfraBadStateException(error);
                }
            }

        } else {
            throw new IllegalStateException("consistency level provided not supported!");
        }
    }


    private Optional<Value<?>> deleteOperation(KvServerClient kvServerClient, String key) throws IOException, ErrorReceivedFromKvServerException {
        System.out.println("will apply delete for key: " + key);

        final String response = kvServerClient.sendMessage(Operations.DELETE.getMsgOp() + " " + key);

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
