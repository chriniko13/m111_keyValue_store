package chriniko.kv.broker.operation;

import chriniko.kv.broker.api.ConsistencyLevel;
import chriniko.kv.broker.api.KvServerContactPoint;
import chriniko.kv.broker.api.QueryKey;
import chriniko.kv.broker.error.KvInfraBadStateException;
import chriniko.kv.broker.error.UncheckedKvInfraBadStateException;
import chriniko.kv.broker.error.availability.NotAtLeastOneKvServerUpException;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KvBrokerQueryOperation {


    public Optional<Value<?>> process(String key, QueryKey queryKey, ConsistencyLevel consistencyLevel,

                                      ConcurrentHashMap<KvServerHealthState, Set<Pair<KvServerClient, KvServerContactPoint>>> kvServerClientsByServerHealthStateStats,
                                      ConcurrentHashMap<KvServerContactPoint, KvServerClient> kvServerClientsByContactPoint,

                                      int replicationFactor,
                                      boolean replicationThresholdSatisfied
    ) throws NotAtLeastOneKvServerUpException, IOException, ErrorReceivedFromKvServerException {


        final String queryKeyString = queryKey.asString();


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
            return queryOperation(client, key, queryKeyString);


        } else if (consistencyLevel == ConsistencyLevel.ALL) {

            // TODO...


        } else if (consistencyLevel == ConsistencyLevel.QUORUM) {

            // TODO...


        } else if (consistencyLevel == ConsistencyLevel.REPLICATION_FACTOR) {

            // TODO...


        } else {
            throw new IllegalStateException("consistency level provided not supported!");
        }


        throw new UnsupportedOperationException("TODO");

    }


    private Optional<Value<?>> queryOperation(KvServerClient kvServerClient, String key, String queryKeyAsString) throws IOException, ErrorReceivedFromKvServerException {
        System.out.println("will query key: " + key + " --- queryKey: " + queryKeyAsString);

        final String response = kvServerClient.sendMessage(Operations.QUERY.getMsgOp() + " " + key + "|" + queryKeyAsString);

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
