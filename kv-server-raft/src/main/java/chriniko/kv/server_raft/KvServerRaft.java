package chriniko.kv.server_raft;

import chriniko.kv.server_raft.heartbeat.Constants;
import chriniko.kv.server_raft.heartbeat.Multicaster;
import chriniko.kv.server_raft.heartbeat.Subscriber;

import java.util.concurrent.CountDownLatch;

public class KvServerRaft {

    private final String serverId;
    private final String networkInterfaceName; // Note: execute `ifconfig` in order to find it.


    public KvServerRaft(String serverId, String networkInterfaceName) {
        this.serverId = serverId;
        this.networkInterfaceName = networkInterfaceName;


        initializeHeartbeat();
    }

    private void initializeHeartbeat() {

        final CountDownLatch endLatch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                endLatch.countDown();
            }
        });


        final Thread multicasterThread = new Thread(() -> {
            new Multicaster(serverId,
                    Constants.Broadcast.MULTICAST_IP, networkInterfaceName, Constants.Broadcast.MULTICAST_PORT,
                    Constants.Schedule.POOL_SIZE).run(endLatch);
        });
        multicasterThread.setName("multicasterThread");
        multicasterThread.setDaemon(false);


        final Thread subscriberThread = new Thread(() -> {
            new Subscriber(serverId, Constants.Broadcast.MULTICAST_IP, networkInterfaceName, Constants.Broadcast.MULTICAST_PORT, Constants.Schedule.POOL_SIZE).run();
        });
        subscriberThread.setName("subscriberThread");
        subscriberThread.setDaemon(false);


        multicasterThread.start();
        subscriberThread.start();

    }

}
