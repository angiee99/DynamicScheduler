package ang.test.schedulertestapp.scheduler;

import java.util.UUID;

public interface SchedulerService {
    // on demand with the no initial delay
    void scheduleOnDemandTask(UUID taskId, Runnable taskLogic);
    // TODO on demand with the initial delay in Cron format
    // TODO scheduled task with Cron
}
