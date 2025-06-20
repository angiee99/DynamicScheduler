package ang.test.schedulertestapp.scheduler;

import ang.test.schedulertestapp.TestTask;
import ang.test.schedulertestapp.timeout.LongRunningTask;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.scheduling.support.CronExpression;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {"task.timeout.value=4", "task.timeout.unit=SECONDS"})
@ExtendWith(OutputCaptureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SchedulerServiceTest {
    @Autowired
    private SchedulerService schedulerService;

    @Test
    @Order(1)
    void scheduleOnDemand(CapturedOutput capturedOutput) throws InterruptedException {
        String message = "I'm so confused";
        TestTask task = new TestTask(message);
        schedulerService.scheduleOnDemandTask(new Random().nextLong(), task);
        Thread.sleep(1000); // wait 1 second for the task execution

        // check for the message in stdout
        assertTrue(capturedOutput.getAll().contains(message));
        // check the iternal counter of the task
        assertEquals(1, task.getCounter());
    }

    @Test
    @Order(2)
    void scheduleOnDemandWithCron(CapturedOutput capturedOutput) throws InterruptedException {
        String message = "My life moves faster than me";
        TestTask task = new TestTask(message);
        schedulerService.scheduleOnDemandTask(
                new Random().nextLong(),
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
    @Order(3)
    void scheduleCronTask(CapturedOutput capturedOutput) throws InterruptedException {
        String message = "Can't feel the ground beneath my feet";
        TestTask task = new TestTask(message);
        schedulerService.scheduleCronTask(
                new Random().nextLong(),
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
    @Order(4)
    void scheduleLongTask(CapturedOutput capturedOutput) throws InterruptedException {
        String message = "Long running task";
        LongRunningTask task = new LongRunningTask(message);
        schedulerService.scheduleOnDemandTask(new Random().nextLong(), task);

        // waiting for the timeout to fire
        Thread.sleep(4500);
        // check that the execution has started
        assertTrue(capturedOutput.getAll().contains(message));

        // check if the task actually did timeout
        assertTrue(capturedOutput.getAll().contains("Task timed out"));
        // check if the interrupt was caught inside the long-running task
        assertTrue(capturedOutput.getAll().contains("Task interrupted"));
    }

    @Test
    @Order(5)
    void cancelCronTaskTest(CapturedOutput capturedOutput) throws InterruptedException {
        String message = "Cron task";
        TestTask task = new TestTask(message);
        Long taskId = new Random().nextLong();
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
    @Order(6)
    void cancelRunningTask(CapturedOutput capturedOutput) throws InterruptedException {
        String message = "Long running task";
        LongRunningTask task = new LongRunningTask(message);
        Long taskId = new Random().nextLong();
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

    @Test
    @Order(7)
    void updatePendingTask(CapturedOutput capturedOutput) throws InterruptedException {
        String originalMessage = "Original task logic";
        String updatedMessage = "Updated task logic";

        TestTask originalTask = new TestTask(originalMessage);
        Long taskId = new Random().nextLong();

        // Schedule the task with some delay so we have time to update it
        schedulerService.scheduleOnDemandTask(taskId, originalTask, CronExpression.parse("*/2 * * * * *")); // runs every 2 seconds

        // Wait a bit less than 2 sec so it's still pending
        Thread.sleep(500);

        // Update the task logic before it fires
        TestTask updatedTask = new TestTask(updatedMessage);
        boolean updated = schedulerService.updateTask(taskId, updatedTask);

        assertTrue(updated, "The update should succeed while task is pending");

        // Now wait long enough for the updated task to execute
        Thread.sleep(2000);

        // Verify the original logic didn't run
        assertFalse(capturedOutput.getAll().contains(originalMessage));
        assertEquals(0, originalTask.getCounter());

        // Verify the updated logic ran
        assertTrue(capturedOutput.getAll().contains(updatedMessage));
        assertEquals(1, updatedTask.getCounter());
    }

    @Test
    @Order(8)
    void updateRunningTaskFails(CapturedOutput capturedOutput) throws InterruptedException {
        String originalMessage = "Running task";
        LongRunningTask task = new LongRunningTask(originalMessage);
        Long taskId = new Random().nextLong();

        // Schedule without delay so it runs immediately
        schedulerService.scheduleOnDemandTask(taskId, task);

        // let the task run for some time
        Thread.sleep(1000);

        // Attempt to update while running
        boolean updated = schedulerService.updateTask(taskId, () -> System.out.println("New logic"));

        assertFalse(updated, "Cannot update task " + taskId + " — task is currently running");

        // Ensure original task still ran
        assertTrue(capturedOutput.getAll().contains(originalMessage));
    }
}
