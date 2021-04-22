package chriniko.kv.server_raft.infra;

public enum HealthNodeState {

    HEALTHY,
    UNHEALTHY,

    SLEEP // Note: if we want to disable a node temporary
}
