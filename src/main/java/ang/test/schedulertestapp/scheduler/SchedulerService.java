package ang.test.schedulertestapp.scheduler;

import org.springframework.scheduling.support.CronExpression;

import java.util.UUID;

public interface SchedulerService {
    // on demand with no initial delay
    void scheduleOnDemandTask(UUID taskId, Runnable taskLogic);

    // on demand with the initial delay in Cron format
    void scheduleOnDemandTask(UUID taskId, Runnable taskLogic, CronExpression delay);

    // TODO scheduled task with Cron
}
