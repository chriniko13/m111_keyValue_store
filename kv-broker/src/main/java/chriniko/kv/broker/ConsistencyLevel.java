package chriniko.kv.broker;

public enum ConsistencyLevel {

    ALL,
    QUORUM, // Quorum = (sum_of_replication_factors / 2) + 1
    ONE

}
