package ang.test.schedulertestapp.scheduler;

import ang.test.schedulertestapp.TestTask;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
class SchedulerServiceTest {
    @Autowired
    private SchedulerService schedulerService;
    @Test
    void scheduleOnDemand(CapturedOutput capturedOutput){
        String message = "I'm so confused";
        schedulerService.scheduleOnDemandTask(UUID.randomUUID(), new TestTask(message));
        assertTrue(capturedOutput.getAll().contains(message));
    }
}
