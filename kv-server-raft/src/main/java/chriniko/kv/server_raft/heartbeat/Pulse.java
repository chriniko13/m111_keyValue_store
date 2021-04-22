package chriniko.kv.server_raft.heartbeat;


import chriniko.kv.server_raft.infra.HealthNodeState;
import lombok.Getter;
import lombok.ToString;

@ToString
final class Pulse {

    @Getter
    private final String serverId;

    @Getter
    private final HealthNodeState healthNodeState;

    @Getter
    private final long receivedTime;


    Pulse(final String serverId, HealthNodeState healthNodeState) {
        this.serverId = serverId;
        this.healthNodeState = healthNodeState;

        this.receivedTime = System.currentTimeMillis();
    }

    boolean isDead(final long tolerance) {
        return System.currentTimeMillis() - this.receivedTime >= tolerance;
    }

    boolean isValid(final long tolerance) {
        return System.currentTimeMillis() - tolerance <= this.receivedTime;
    }
}
