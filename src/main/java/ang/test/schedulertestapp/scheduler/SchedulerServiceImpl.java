package ang.test.schedulertestapp.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class SchedulerServiceImpl implements SchedulerService{
    private final TaskScheduler taskScheduler;
    private final Map<UUID, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();
    private final long defaultTimeout;
    private final TimeUnit defaultTimeoutUnit;
    @Autowired
    public SchedulerServiceImpl(
            ThreadPoolTaskScheduler threadPoolTaskScheduler,
            @Value("${task.timeout.value:1}") long defaultTimeout,
            @Value("#{T(java.util.concurrent.TimeUnit).SECONDS}") TimeUnit defaultTimeoutUnit) {
        this.taskScheduler = threadPoolTaskScheduler;
        this.defaultTimeout = defaultTimeout;
        this.defaultTimeoutUnit = defaultTimeoutUnit;
    }

    // TODO -> a mechanism for reading tasks from DB at application start is still required
    @Override
    public void scheduleOnDemandTask(UUID taskId, Runnable taskLogic) {
        // schedule the task with no delay
        ScheduledFuture<?> future = taskScheduler.schedule(taskLogic, Instant.now());

        // store the task locally for dynamic changes
        scheduledTasks.put(taskId, future);
    }
    public void scheduleOnDemandTask(UUID taskId, Runnable taskLogic, CronExpression delay) {
        // TODO wrap the initial task logic with timeout handling
//        Runnable wrapped = wrapWithTimeout(taskLogic, defaultTimeout, defaultTimeoutUnit);

        // schedule the task with delay as the cron was provided
        ScheduledFuture<?> future = taskScheduler.schedule(taskLogic, delay.next(Instant.now()));

        // store the task locally for dynamic changes
        scheduledTasks.put(taskId, future);

        // TODO if needed, persist the task, e.g. to DB
    }

}
