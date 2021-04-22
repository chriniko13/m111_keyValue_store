package chriniko.kv.server_raft;

import java.util.UUID;

public class PoC {


    public static void main(String[] args) {
        //TODO work on multicast, heartbeat, leader-election.....

        KvServerRaft kvServerRaft = new KvServerRaft(UUID.randomUUID().toString(), "wlp3s0");


        for(;;);

    }

}
