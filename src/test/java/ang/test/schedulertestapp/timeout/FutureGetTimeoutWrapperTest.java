package ang.test.schedulertestapp.timeout;

import ang.test.schedulertestapp.TestTask;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FutureGetTimeoutWrapperTest {
    @Qualifier("futureGetTimeoutWrapper")
    @Autowired
    private TimeoutWrapper futureGetTimeoutWrapper;

    private final String TIMEOUT_MESSAGE = "Task timed out";

    @Test
    @Order(1)
    void taskFinishesBeforeTimeout(CapturedOutput capturedOutput) throws ExecutionException, InterruptedException {
        String message = "Apfel Shorle";
        Future<?> future = futureGetTimeoutWrapper.wrap(new TestTask(message), 2, TimeUnit.SECONDS);
        future.get();
        assertTrue(capturedOutput.getAll().contains(message));
        assertFalse(capturedOutput.getAll().contains(TIMEOUT_MESSAGE));
    }

    @Test
    @Order(2)
    void taskTimesOut(CapturedOutput capturedOutput) {
        String message = "Zhyvchick";
        Future<?> future = futureGetTimeoutWrapper.wrap(new LongRunningTask(message), 2, TimeUnit.SECONDS);

        // check that the future got canceled
        assertThrows(CancellationException.class, future::get);

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
