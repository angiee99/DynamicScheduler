package ang.test.schedulertestapp.scheduler;

import org.springframework.scheduling.support.CronExpression;

/**
 * The service handles the scheduling of tasks.
 * It should enable scheduling on-demand and reoccurring tasks on the run, as well as cancelling and updating them.
 */
public interface SchedulerService {
    // on demand with no initial delay
    void scheduleOnDemandTask(Long taskId, Runnable taskLogic);

    // on demand with the initial delay in Cron format
    void scheduleOnDemandTask(Long taskId, Runnable taskLogic, CronExpression delay);

    // scheduled task with Cron
    void scheduleCronTask(Long taskId, Runnable taskLogic, CronExpression expression);

    // cancel the task
    boolean cancelTask(Long taskId);

    // update the task
    boolean updateTask(Long taskId, Runnable newTaskLogic);
}
