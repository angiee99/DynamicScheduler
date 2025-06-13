package ang.test.schedulertestapp.timeout;

import org.springframework.stereotype.Service;

import java.util.concurrent.*;
@Service
public class NonBlockingTimeoutWrapper implements TimeoutWrapper{
    @Override
    public Runnable wrap(Runnable runnable, long timeout, TimeUnit timeUnit) {
        return () -> {
            try (ScheduledExecutorService executor = Executors.newScheduledThreadPool(2)) {
                Future<?> future = executor.submit(runnable);
                Runnable cancelTask = () -> future.cancel(true);

                executor.schedule(cancelTask, 3000, TimeUnit.MILLISECONDS);
                executor.shutdown();
            }
        };
    }
}
