package ang.test.schedulertestapp.scheduler;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
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

    // TODO per task -> wrapping the tasks for timeout handling
    // TODO per task -> if needed, persist the task, e.g. to DB

    @Override
    public void scheduleOnDemandTask(UUID taskId, Runnable taskLogic) {
        // schedule the task with no delay
        ScheduledFuture<?> future = taskScheduler.schedule(taskLogic, Instant.now());

        // store the task locally for dynamic changes
        scheduledTasks.put(taskId, future);
    }

    @Override
    public void scheduleOnDemandTask(UUID taskId, Runnable taskLogic, CronExpression cronExpression) {
        // calculate the delay based on the provided cron expression
        LocalDateTime nextExecution = cronExpression.next(LocalDateTime.now());
        assert nextExecution != null; // not sure how this works when the value is null actually
        LocalDateTime nextToNextExecution = cronExpression.next(nextExecution);
        Duration durationBetweenExecutions = Duration.between(
                nextExecution, nextToNextExecution
        );
        // schedule the task with delay as the cron was provided
        ScheduledFuture<?> future = taskScheduler.schedule(taskLogic, Instant.now().plus(
                durationBetweenExecutions));

        // store the task locally for dynamic changes
        scheduledTasks.put(taskId, future);
    }

    @Override
    public void scheduleCronTask(UUID taskId, Runnable taskLogic, CronExpression expression) {
        // schedule the task with delay as the cron was provided
        ScheduledFuture<?> future = taskScheduler.schedule(taskLogic, new CronTrigger(expression.toString()));

        // store the task locally for dynamic changes
        scheduledTasks.put(taskId, future);
    }

    @PreDestroy
    public void shutdown() {
        // Cancel all scheduled tasks
        System.out.println("Shutting down the Scheduler service...");
        scheduledTasks.values().forEach(future -> future.cancel(true));
        scheduledTasks.clear();
    }

}
