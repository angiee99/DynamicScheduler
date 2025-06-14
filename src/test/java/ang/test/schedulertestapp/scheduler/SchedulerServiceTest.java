package ang.test.schedulertestapp.scheduler;

import ang.test.schedulertestapp.TestTask;
import ang.test.schedulertestapp.timeout.LongRunningTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.scheduling.support.CronExpression;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"task.timeout.value=4", "task.timeout.unit=SECONDS"})
@ExtendWith(OutputCaptureExtension.class)
// todo add order still
class SchedulerServiceTest {
    @Autowired
    private SchedulerService schedulerService;

    @Test
    void scheduleOnDemand(CapturedOutput capturedOutput) throws InterruptedException {
        String message = "I'm so confused";
        TestTask task = new TestTask(message);
        schedulerService.scheduleOnDemandTask(UUID.randomUUID(), task);
        Thread.sleep(1000); // wait 1 second for the task execution

        // check for the message in stdout
        assertTrue(capturedOutput.getAll().contains(message));
        // check the iternal counter of the task
        assertEquals(1, task.getCounter());
    }

    @Test
    void scheduleOnDemandWithCron(CapturedOutput capturedOutput) throws InterruptedException {
        String message = "My life moves faster than me";
        TestTask task = new TestTask(message);
        schedulerService.scheduleOnDemandTask(
                UUID.randomUUID(),
                task,
                CronExpression.parse("*/2 * * * * *")); // 2-second delay
        // test that it does not fire immediately
        Thread.sleep(1000);
        assertFalse(capturedOutput.getAll().contains(message));

        // wait for a task to run
        Thread.sleep(1500);  // wait 1.5 seconds — enough for the first cron tick
        assertTrue(capturedOutput.getAll().contains(message));
        assertEquals(1, task.getCounter());
    }

    @Test
    void scheduleCronTask(CapturedOutput capturedOutput) throws InterruptedException {
        String message = "Can't feel the ground beneath my feet";
        TestTask task = new TestTask(message);
        schedulerService.scheduleCronTask(
                UUID.randomUUID(),
                task,
                CronExpression.parse("*/2 * * * * *")); // every 2 seconds

        // wait for a task to run for the first time
        Thread.sleep(2000);
        assertTrue(capturedOutput.getAll().contains(message));

        // wait for a task to run for the second time
        Thread.sleep(2000); // wait 2 seconds for the next run to check periodic execution
        assertEquals(2, task.getCounter());
    }

    @Test
    void scheduleLongTask(CapturedOutput capturedOutput) throws InterruptedException {
        String message = "Long running task";
        LongRunningTask task = new LongRunningTask(message);
        schedulerService.scheduleOnDemandTask(UUID.randomUUID(), task);

        // waiting for the timeout to fire
        Thread.sleep(4000);
        // check that the execution has started
        assertTrue(capturedOutput.getAll().contains(message));

        // check if the task actually did timeout
        assertTrue(capturedOutput.getAll().contains("Task timed out"));
        // check if the interrupt was caught inside the long-running task
        assertTrue(capturedOutput.getAll().contains("Task interrupted"));
    }

    @Test
    void cancelCronTaskTest(CapturedOutput capturedOutput) throws InterruptedException {
        String message = "Cron task";
        TestTask task = new TestTask(message);
        UUID taskId = UUID.randomUUID();
        schedulerService.scheduleCronTask(
                taskId,
                task,
                CronExpression.parse("*/2 * * * * *")); // every 2 seconds

        // wait for a task to run for the first time
        Thread.sleep(2000);
        assertTrue(capturedOutput.getAll().contains(message));

        assertTrue(schedulerService.cancelTask(taskId));
        assertTrue(capturedOutput.getAll().contains("task with id " + taskId + " was cancelled"));

        // wait to check if the task does not fire and is truly canceled
        Thread.sleep(2000);
        assertEquals(1, task.getCounter());
    }
    @Test
    void cancelRunningTask(CapturedOutput capturedOutput) throws InterruptedException {
        String message = "Long running task";
        LongRunningTask task = new LongRunningTask(message);
        UUID taskId = UUID.randomUUID();
        schedulerService.scheduleOnDemandTask(taskId, task);

        // let the task run for some time
        Thread.sleep(1000);
        // check that the execution has started
        assertTrue(capturedOutput.getAll().contains(message));

        // cancel the task while it is running
        assertTrue(schedulerService.cancelTask(taskId));
        assertTrue(capturedOutput.getAll().contains("task with id " + taskId + " was cancelled"));

        // check if the interrupt was caught inside the long-running task
        assertTrue(capturedOutput.getAll().contains("Task interrupted"));
    }
}
