package chriniko.kv.server_raft.infra;


/*
    TODO implement RAFT
 */
public enum RaftNodeState {

    FOLLOWER,
    CANDIDATE,
    LEADER;

    /*

    The candidate becomes the leader if it gets votes from a majority of nodes.
This process is called Leader Election.
All changes to the system now go through the leader.



Log Replication

     */
}
