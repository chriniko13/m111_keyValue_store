package chriniko.kv.server_raft.heartbeat;

public final class Constants {

    // =================================================================================================================

    public static final class Broadcast {

        /*
            https://www.iana.org/assignments/multicast-addresses/multicast-addresses.xhtml

                The multicast addresses are in the range
                224.0.0.0 through 239.255.255.255. Address assignments are listed below.

                The range of addresses between 224.0.0.0 and 224.0.0.255, inclusive,
                is reserved for the use of routing protocols and other low-level
                topology discovery or maintenance protocols, such as gateway discovery
                and group membership reporting.  Multicast routers should not forward
                any multicast datagram with destination addresses in this range,
                regardless of its TTL.

         */
        public static final String MULTICAST_IP = "239.1.1.1";
        public static final int MULTICAST_PORT = 9999;

        private Broadcast() {
        }
    }

    // =================================================================================================================

    public static final class Schedule {

        public static final long DOWNTIME_TOLERANCE_IN_MILLISECONDS = 15_000;
        public static final long DOWNTIME_TOLERANCE_DEAD_SERVICE_IN_MILLISECONDS = 30_000;
        public static final long PULSE_DELAY_IN_MILLISECONDS = 5_000;

        public static final int POOL_SIZE = 1;

        private Schedule() {
        }
    }


    // =================================================================================================================

    private Constants() {
    }

}
