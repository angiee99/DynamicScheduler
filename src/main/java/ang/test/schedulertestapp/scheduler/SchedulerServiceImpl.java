package ang.test.schedulertestapp.scheduler;

import ang.test.schedulertestapp.timeout.TimeoutWrapper;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * It stores the scheduled tasks in a {@link ConcurrentHashMap} in format of {@link ScheduledTaskHandle} instances.
 * It should also take care of limiting the task execution with the use of {@link TimeoutWrapper}.
 */
@Service
public class SchedulerServiceImpl implements SchedulerService {

    private final ThreadPoolTaskScheduler taskScheduler;
    private final TimeoutWrapper timeoutWrapper;
    private final long defaultTimeout;
    private final TimeUnit defaultTimeoutUnit;
    private final Map<UUID, ScheduledTaskHandle> scheduledTasks = new ConcurrentHashMap<>();

    @Autowired
    public SchedulerServiceImpl(
            ThreadPoolTaskScheduler threadPoolTaskScheduler,
            @Qualifier("nonBlockingTimeoutWrapper") TimeoutWrapper timeoutWrapper,
            @Value("${task.timeout.value:60}") long defaultTimeout,
            @Value("#{T(java.util.concurrent.TimeUnit).SECONDS}") TimeUnit defaultTimeoutUnit) {
        this.taskScheduler = threadPoolTaskScheduler;
        this.timeoutWrapper = timeoutWrapper;
        this.defaultTimeout = defaultTimeout;
        this.defaultTimeoutUnit = defaultTimeoutUnit;
    }

    @Override
    public void scheduleOnDemandTask(UUID taskId, Runnable taskLogic) {
        ScheduledTaskHandle handle = new ScheduledTaskHandle();
        ScheduledFuture<?> triggerFuture = taskScheduler.schedule(
                () -> {
                    Future<?> runningFuture = timeoutWrapper.wrap(taskLogic, defaultTimeout, defaultTimeoutUnit);
                    handle.setRunningTaskFuture(runningFuture);},
                Instant.now());

        handle.setScheduledTrigger(triggerFuture);
        scheduledTasks.put(taskId, handle);
    }

    @Override
    public void scheduleOnDemandTask(UUID taskId, Runnable taskLogic, CronExpression cronExpression) {
        ScheduledTaskHandle handle = new ScheduledTaskHandle();
        // calculate the delay based on the provided cron expression
        LocalDateTime nextExecution = cronExpression.next(LocalDateTime.now());
        if (nextExecution == null) {
            throw new IllegalArgumentException("Invalid cron expression: no next execution time");
        }
        LocalDateTime nextToNextExecution = cronExpression.next(nextExecution);
        Duration durationBetweenExecutions = Duration.between(
                nextExecution, nextToNextExecution
        );
        // schedule the task with delay as the cron was provided
        ScheduledFuture<?> triggerFuture = taskScheduler.schedule(
                () -> {
                    Future<?> runningFuture = timeoutWrapper.wrap(taskLogic, defaultTimeout, defaultTimeoutUnit);
                    handle.setRunningTaskFuture(runningFuture);},
                Instant.now().plus(durationBetweenExecutions));

        handle.setScheduledTrigger(triggerFuture);
        scheduledTasks.put(taskId, handle);
    }

    @Override
    public void scheduleCronTask(UUID taskId, Runnable taskLogic, CronExpression expression) {
        ScheduledTaskHandle handle = new ScheduledTaskHandle();
        ScheduledFuture<?> triggerFuture = taskScheduler.schedule(
                () -> {
                    Future<?> runningFuture = timeoutWrapper.wrap(taskLogic, defaultTimeout, defaultTimeoutUnit);
                    handle.setRunningTaskFuture(runningFuture);},
                new CronTrigger(expression.toString()));

        handle.setScheduledTrigger(triggerFuture);
        scheduledTasks.put(taskId, handle);
    }

    /**
     * Cancels both currently running execution (if any) and the future-scheduled firings (if any)
     * @param taskId id of a task to be canceled
     * @return true if a task was canceled
     */
    @Override
    public boolean cancelTask(UUID taskId) {
        // cancel the running task if any
        boolean canceledRunning = cancelRunningInstance(taskId);

        // cancel any possible future firings
        boolean canceledFutureFirings = cancelFutureFirings(taskId);

        // remove the task from the registry
        scheduledTasks.remove(taskId);
        return canceledRunning && canceledFutureFirings;
    }

    private boolean cancelRunningInstance(UUID taskId){
        ScheduledTaskHandle handle = scheduledTasks.get(taskId);
        if (handle != null) {
            if (handle.getRunningTaskFuture() != null) {
                handle.getRunningTaskFuture().cancel(true);
                System.out.println("Running task cancelled");
            }
            return true;
        }
        return false;
    }

    boolean cancelFutureFirings(UUID taskId){
        ScheduledTaskHandle handle = scheduledTasks.get(taskId);
        if (handle != null) {
            if (handle.getScheduledTrigger() != null) {
                handle.getScheduledTrigger().cancel(true);
                System.out.println("Scheduled trigger of task with id " + taskId + " was cancelled");
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean updateTask(UUID taskId, Runnable newTaskLogic) {
        ScheduledTaskHandle handle = scheduledTasks.get(taskId);
        if (handle == null) {
            System.out.println("No task found with id: " + taskId);
            return false;
        }

        // check that the task is not currently running
        Future<?> runningFuture = handle.getRunningTaskFuture();
        if (runningFuture != null && !runningFuture.isDone()) {
            System.out.println("Cannot update task " + taskId + " — task is currently running");
            return false;
        }

        // cancel the scheduled trigger (if not already fired)
        ScheduledFuture<?> scheduledFuture = handle.getScheduledTrigger();
        if (scheduledFuture != null && !scheduledFuture.isDone()) {
            scheduledFuture.cancel(false);  // don't interrupt if somehow started
        }

        // TODO reschedule differently based on task type - on-demand with/without delay, cron task
        // reschedule with the new logic at the same scheduled time (if available)
        // for simplicity, reschedule immediately — or you can enhance to reuse timing data if you store it
        ScheduledFuture<?> newTrigger = taskScheduler.schedule(() -> {
            Future<?> newRunning = timeoutWrapper.wrap(newTaskLogic, defaultTimeout, defaultTimeoutUnit);
            handle.setRunningTaskFuture(newRunning);
        }, Instant.now());

        handle.setScheduledTrigger(newTrigger);
        System.out.println("Task " + taskId + " updated and rescheduled");
        return true;
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
