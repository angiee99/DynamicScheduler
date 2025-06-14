package ang.test.schedulertestapp.scheduler;

import org.springframework.stereotype.Service;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
@Service
public class ScheduledTaskHandle {
    private ScheduledFuture<?> scheduledTrigger;
    private volatile Future<?> runningTaskFuture;

    public ScheduledFuture<?> getScheduledTrigger() {
        return scheduledTrigger;
    }

    public void setScheduledTrigger(ScheduledFuture<?> scheduledTrigger) {
        this.scheduledTrigger = scheduledTrigger;
    }

    public Future<?> getRunningTaskFuture() {
        return runningTaskFuture;
    }

    public void setRunningTaskFuture(Future<?> runningTaskFuture) {
        this.runningTaskFuture = runningTaskFuture;
    }
}
