package chriniko.kv.broker.operation;

import chriniko.kv.broker.api.ConsistencyLevel;
import chriniko.kv.broker.api.KvServerContactPoint;
import chriniko.kv.broker.api.QueryKey;
import chriniko.kv.broker.error.availability.NotAtLeastOneKvServerUpException;
import chriniko.kv.broker.health.KvServerHealthState;
import chriniko.kv.broker.infra.KvServerClient;
import chriniko.kv.broker.infra.SetUtils;
import chriniko.kv.datatypes.Value;
import org.javatuples.Pair;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KvBrokerQueryOperation {


    public Optional<Value<?>> process(QueryKey key, ConsistencyLevel consistencyLevel,

                                      ConcurrentHashMap<KvServerHealthState, Set<Pair<KvServerClient, KvServerContactPoint>>> kvServerClientsByServerHealthStateStats,
                                      ConcurrentHashMap<KvServerContactPoint, KvServerClient> kvServerClientsByContactPoint,

                                      int replicationFactor,
                                      boolean replicationThresholdSatisfied
    ) throws NotAtLeastOneKvServerUpException {

        if (consistencyLevel == ConsistencyLevel.ONE) {

            // TODO...
            // check servers state
            final Set<Pair<KvServerClient, KvServerContactPoint>> r = kvServerClientsByServerHealthStateStats.get(KvServerHealthState.UP);
            if (r == null || r.isEmpty()) {
                throw new NotAtLeastOneKvServerUpException("not at least one kv-server is up and running");
            }


            // pick correct ones and process
            final Pair<KvServerClient, KvServerContactPoint> selection = SetUtils.pickOneRandomly(r);

            System.out.println("will execute delete operation against kv-server: " + selection.getValue1());

            final KvServerClient client = selection.getValue0();
            //return queryOperation(client, key);


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

}
