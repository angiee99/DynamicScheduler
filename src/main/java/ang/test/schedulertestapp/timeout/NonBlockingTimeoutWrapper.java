package ang.test.schedulertestapp.timeout;

import org.springframework.stereotype.Service;

import java.util.concurrent.*;
@Service
public class NonBlockingTimeoutWrapper implements TimeoutWrapper{
    // TODO fix: does not work correct with Cron tasks
    @Override
    public Runnable wrap(Runnable runnable, long timeout, TimeUnit timeUnit) {
        return () -> {
            try (ScheduledExecutorService executor = Executors.newScheduledThreadPool(2)) {
                Future<?> future = executor.submit(runnable);
                Runnable cancelTask = () -> {
                    future.cancel(true);
                    if(future.isCancelled()){
                        System.out.println("Task timed out. CancelTask is called");
                    }
                };

                executor.schedule(cancelTask, timeout, timeUnit);
                executor.shutdown();
            }
        };
    }
}
