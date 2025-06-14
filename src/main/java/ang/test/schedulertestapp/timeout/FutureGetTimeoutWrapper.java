package ang.test.schedulertestapp.timeout;

import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class FutureGetTimeoutWrapper implements TimeoutWrapper{
    @Override
    public Runnable wrap(Runnable runnable, long timeout, TimeUnit timeUnit) {
        return () -> {
            try (ExecutorService executor = Executors.newSingleThreadExecutor()) {
                Future<?> future = executor.submit(runnable);
                try {
                    // limit the wait for the result
                    future.get(timeout, timeUnit);
                } catch (TimeoutException | InterruptedException e) {
                    System.out.println("Task timed out with exception: " + e.getClass());
                    future.cancel(true);
                    // ? should we throw sth here
                } catch (Exception e) {
                    System.out.println("Task ended with exception: " + e.getClass() + e.getMessage());
                    e.printStackTrace();
                } finally {
                    executor.shutdownNow();
                }
            }
        };
    }
}
