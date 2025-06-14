package ang.test.schedulertestapp.scheduler;

import ang.test.schedulertestapp.timeout.TimeoutWrapper;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * Service that manages task scheduling - creation, cancellation, updates.
 * It stores the scheduled tasks in a {@link ConcurrentHashMap} in format of {@link ScheduledFuture} instances.
 * It should also take care of limiting the task execution with the use of {@link TimeoutWrapper}.
 */
@Service
public class SchedulerServiceImpl implements SchedulerService {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final ScheduledExecutorService taskExecutor;
    private final long defaultTimeout;
    private final TimeUnit defaultTimeoutUnit;
    private final Map<UUID, ScheduledTaskHandle> scheduledTasks = new ConcurrentHashMap<>();

    @Autowired
    public SchedulerServiceImpl(
            ThreadPoolTaskScheduler threadPoolTaskScheduler,
            ScheduledExecutorService taskExecutor,
            @Value("${task.timeout.value:60}") long defaultTimeout,
            @Value("#{T(java.util.concurrent.TimeUnit).SECONDS}") TimeUnit defaultTimeoutUnit) {
        this.taskScheduler = threadPoolTaskScheduler;
        this.taskExecutor = taskExecutor;
        this.defaultTimeout = defaultTimeout;
        this.defaultTimeoutUnit = defaultTimeoutUnit;
    }

    @Override
    public void scheduleOnDemandTask(UUID taskId, Runnable taskLogic) {
        ScheduledTaskHandle handle = new ScheduledTaskHandle();
        ScheduledFuture<?> triggerFuture = taskScheduler.schedule(() -> {
            Future<?> runningFuture = runWithTimeout(taskLogic, defaultTimeout, defaultTimeoutUnit);
            handle.setRunningTaskFuture(runningFuture);
        }, Instant.now());

        handle.setScheduledTrigger(triggerFuture);
        scheduledTasks.put(taskId, handle);
    }

    @Override
    public void scheduleOnDemandTask(UUID taskId, Runnable taskLogic, CronExpression cronExpression) {
        ScheduledTaskHandle handle = new ScheduledTaskHandle();
        // calculate the delay based on the provided cron expression
        LocalDateTime nextExecution = cronExpression.next(LocalDateTime.now());
        assert nextExecution != null; // not sure how this works when the value is null actually
        LocalDateTime nextToNextExecution = cronExpression.next(nextExecution);
        Duration durationBetweenExecutions = Duration.between(
                nextExecution, nextToNextExecution
        );
        // schedule the task with delay as the cron was provided
        ScheduledFuture<?> triggerFuture = taskScheduler.schedule(
                () -> {
                    Future<?> runningFuture = runWithTimeout(taskLogic, defaultTimeout, defaultTimeoutUnit);
                    handle.setRunningTaskFuture(runningFuture);},
                Instant.now().plus(durationBetweenExecutions));

        handle.setScheduledTrigger(triggerFuture);
        scheduledTasks.put(taskId, handle);
    }

    @Override
    public void scheduleCronTask(UUID taskId, Runnable taskLogic, CronExpression expression) {
        ScheduledTaskHandle handle = new ScheduledTaskHandle();
        ScheduledFuture<?> triggerFuture = taskScheduler.schedule(() -> {
            Future<?> runningFuture = runWithTimeout(taskLogic, defaultTimeout, defaultTimeoutUnit);
            handle.setRunningTaskFuture(runningFuture);
        }, new CronTrigger(expression.toString()));

        handle.setScheduledTrigger(triggerFuture);
        scheduledTasks.put(taskId, handle);
    }

    @Override
    public boolean cancelTask(UUID taskId) {
        ScheduledTaskHandle handle = scheduledTasks.remove(taskId);
        if (handle != null) {
            if (handle.getRunningTaskFuture() != null) {
                handle.getRunningTaskFuture().cancel(true);
                System.out.println("Running task cancelled");
            }
            if (handle.getScheduledTrigger() != null) {
                handle.getScheduledTrigger().cancel(true);
                System.out.println("Scheduled trigger of task with id " + taskId + " was cancelled");
            }
            return true;
        }
        return false;
    }

    // todo use NonBlockingTimeoutWrapper for it (seems to be same)
    private Future<?> runWithTimeout(Runnable taskLogic, long timeout, TimeUnit unit) {
        Future<?> future = taskExecutor.submit(taskLogic);
        taskExecutor.schedule(() -> {
            if (!future.isDone()) {
                future.cancel(true);
                System.out.println("Task timed out. CancelTask is called");
            }
        }, timeout, unit);
        return future;
    }

    @PreDestroy
    public void shutdown() {
        System.out.println("Shutting down the Scheduler service...");
        scheduledTasks.values().forEach(handle -> {
            if (handle.getRunningTaskFuture() != null) {
                handle.getRunningTaskFuture().cancel(true);
            }
            if (handle.getScheduledTrigger() != null) {
                handle.getScheduledTrigger().cancel(true);
            }
        });
        scheduledTasks.clear();
    }
}
