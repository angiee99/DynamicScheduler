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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class SchedulerServiceTest {
    @Autowired
    private SchedulerService schedulerService;

    @Test
    @Order(1)
    void scheduleOnDemand(CapturedOutput capturedOutput) throws InterruptedException {
        String message = "I'm so confused";
        schedulerService.scheduleOnDemandTask(UUID.randomUUID(), new TestTask(message));
        Thread.sleep(1000); // wait 1 second for the task execution
        assertTrue(capturedOutput.getAll().contains(message));
    }

    @Test
    @Order(2)
    void scheduleOnDemandWithCron(CapturedOutput capturedOutput) throws InterruptedException {
        String message = "My life moves faster than me";
        schedulerService.scheduleOnDemandTask(
                UUID.randomUUID(),
                new TestTask(message),
                CronExpression.parse("*/2 * * * * *")); // 2-second delay
        // test that it does not fire immediately
        Thread.sleep(1000);
        assertFalse(capturedOutput.getAll().contains(message));

        // wait for a task to run
        Thread.sleep(1500);  // wait 1.5 seconds — enough for the first cron tick
        assertTrue(capturedOutput.getAll().contains(message));
    }
}
