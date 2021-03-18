package chriniko.kv.server.raft;


/*
    TODO implement RAFT
 */
public enum NodeState {

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
