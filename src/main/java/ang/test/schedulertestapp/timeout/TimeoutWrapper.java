package ang.test.schedulertestapp.timeout;

import java.util.concurrent.TimeUnit;

/**
 * Limits the time of a {@link Runnable]} execution by wrapping it in specific time-limit handling.
 */
public interface TimeoutWrapper {
    Runnable wrap(Runnable runnable, long timeout, TimeUnit timeUnit);
}
