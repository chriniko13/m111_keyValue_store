package chriniko.kv.server_raft.heartbeat;

import java.nio.channels.DatagramChannel;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public interface ScheduledChannelOperation {

    default void doSchedule(DatagramChannel channel, Runnable runnable, long initialDelay, long delay, TimeUnit unit) {
        if (Objects.isNull(channel) && Objects.isNull(runnable) && Objects.isNull(unit)) {
            throw new IllegalArgumentException("channel, runnable and unit required");
        }

        getService().scheduleWithFixedDelay(runnable, initialDelay, Constants.Schedule.PULSE_DELAY_IN_MILLISECONDS, TimeUnit.MILLISECONDS);
    }

    ScheduledExecutorService getService();
}
