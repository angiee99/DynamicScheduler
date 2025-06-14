package ang.test.schedulertestapp.timeout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;
@Service
public class NonBlockingTimeoutWrapper implements TimeoutWrapper{
    private final ScheduledExecutorService executor;
    @Autowired
    public NonBlockingTimeoutWrapper(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public Future<?> wrap(Runnable runnable, long timeout, TimeUnit timeUnit) {
        Future<?> future = executor.submit(runnable);

        executor.schedule(() -> {
            if (!future.isDone()) {
                future.cancel(true);
                System.out.println("Task timed out. CancelTask is called");
            }
        }, timeout, timeUnit);

        return future;
    }
}
