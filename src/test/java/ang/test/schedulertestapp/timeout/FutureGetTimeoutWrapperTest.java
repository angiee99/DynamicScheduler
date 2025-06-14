package ang.test.schedulertestapp.timeout;

import ang.test.schedulertestapp.TestTask;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
public class FutureGetTimeoutWrapperTest {
    @Qualifier("futureGetTimeoutWrapper")
    @Autowired
    private TimeoutWrapper futureGetTimeoutWrapper;

    private final String TIMEOUT_MESSAGE = "Task timed out";

    @Test
    @Order(1)
    void taskFinishesBeforeTimeout(CapturedOutput capturedOutput) {
        String message = "Apfel Shorle";
        Runnable runnable = futureGetTimeoutWrapper.wrap(new TestTask(message), 2, TimeUnit.SECONDS);
        runnable.run();
        assertTrue(capturedOutput.getAll().contains(message));
        assertFalse(capturedOutput.getAll().contains(TIMEOUT_MESSAGE));
    }

    @Test
    @Order(2)
    void taskTimesOut(CapturedOutput capturedOutput) {
        String message = "Zhyvchick";
        Runnable runnable = futureGetTimeoutWrapper.wrap(new LongRunningTask(message), 2, TimeUnit.SECONDS);
        runnable.run();
        // verify the start of the task execution
        assertTrue(capturedOutput.getAll().contains(message));
        // check if the task actually did timeout
        assertTrue(capturedOutput.getAll().contains(TIMEOUT_MESSAGE));
        // check for specific TimeoutException for the futureGetTimeoutWrapper
        assertTrue(capturedOutput.getAll().contains(TimeoutException.class.getName()));
        // check if the interrupt was caught inside the long-running task
        assertTrue(capturedOutput.getAll().contains("Task interrupted"));
    }

}
