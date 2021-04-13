package chriniko.kv.broker;

import chriniko.kv.broker.error.availability.*;
import chriniko.kv.broker.error.response.ErrorReceivedFromKvServerException;
import chriniko.kv.datatypes.Value;
import chriniko.kv.protocol.NotOkayResponseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface KvBrokerApi {


    // --- infra to start/stop and check state of broker ---
    void start(List<KvServerContactPoint> kvServerContactPoints,
               BufferedReader dataToIndexBufferedReader,
               boolean injectGeneratedData,
               int replicationFactor,
               Runnable readyAction) throws NotOkayResponseException, IOException, ErrorReceivedFromKvServerException;

    void stop();

    boolean getReplicationThresholdSatisfied();

    Map<KvServerContactPoint, KvServerClient> getKvServerClientsByContactPoint();




    // --- operations supported ---


    void put(String key, Value<?> value, ConsistencyLevel consistencyLevel) throws KvServerAvailabilityException, IOException, ErrorReceivedFromKvServerException;


    void putRaw(String key, String value, ConsistencyLevel consistencyLevel) throws KvServerAvailabilityException, IOException, ErrorReceivedFromKvServerException;


    Optional<Value<?>> get(String key, ConsistencyLevel consistencyLevel) throws NotAtLeastOneKvServerUpException, IOException, ErrorReceivedFromKvServerException, NotAllKvServersAreUpException,
            QuorumNotApplicableException, ReplicationFactorNotApplicableException;


    Optional<Value<?>> delete(String key, ConsistencyLevel consistencyLevel) throws IOException, ErrorReceivedFromKvServerException, NotAtLeastOneKvServerUpException, NotAllKvServersAreUpException,
            ReplicationFactorNotApplicableException, QuorumNotApplicableException;


    Optional<Value<?>> query(String key, ConsistencyLevel consistencyLevel);
}
