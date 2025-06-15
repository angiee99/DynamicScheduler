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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class NonBlockingTimeoutWrapperTest {
    @Qualifier("nonBlockingTimeoutWrapper")
    @Autowired
    private TimeoutWrapper nonBlockingTimeoutWrapper;

    private final String TIMEOUT_MESSAGE = "Task timed out";

    @Test
    @Order(1)
    void taskFinishesBeforeTimeout(CapturedOutput capturedOutput) throws ExecutionException, InterruptedException {
        String message = "Apfel Shorle";
        Future<?> runnable = nonBlockingTimeoutWrapper.wrap(new TestTask(message), 2, TimeUnit.SECONDS);
        runnable.get();
        assertTrue(capturedOutput.getAll().contains(message));
        assertFalse(capturedOutput.getAll().contains(TIMEOUT_MESSAGE));
    }

//    @Test
//    @Order(2)
//    void taskTimesOutNonBlocking(CapturedOutput capturedOutput) throws InterruptedException {
//        String message = "Zhyvchick";
//        Runnable runnable = nonBlockingTimeoutWrapper.wrap(new LongRunningTask(message), 2, TimeUnit.SECONDS);
//        runnable.run();
//        // wait for the timeout to fire
//        Thread.sleep(2500);
//        // verify the start of the task execution
//        assertTrue(capturedOutput.getAll().contains(message));
//        // check if the task actually did timeout
//        assertTrue(capturedOutput.getAll().contains(TIMEOUT_MESSAGE));
//        // check for a specific message from nonBlockingTimeoutWrapper
//        assertTrue(capturedOutput.getAll().contains("CancelTask is called"));
//        // check if the interrupt was caught inside the long-running task
//        assertTrue(capturedOutput.getAll().contains("Task interrupted"));
//    }
}
