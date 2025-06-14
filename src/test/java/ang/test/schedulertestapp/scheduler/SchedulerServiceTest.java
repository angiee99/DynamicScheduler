package ang.test.schedulertestapp.scheduler;

import ang.test.schedulertestapp.TestTask;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.scheduling.support.CronExpression;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class SchedulerServiceTest {
    @Autowired
    private SchedulerService schedulerService;

    @Test
    @Order(1)
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
    @Order(2)
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
    @Order(3)
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
}
