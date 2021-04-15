package chriniko.kv.broker.api;

public enum ConsistencyLevel {

    ALL,
    QUORUM, // Quorum = (sum_of_replication_factors / 2) + 1
    REPLICATION_FACTOR,
    ONE;

    public static int calculateQuorum(int replicationFactor) {
        return (replicationFactor / 2) + 1;
    }
}
