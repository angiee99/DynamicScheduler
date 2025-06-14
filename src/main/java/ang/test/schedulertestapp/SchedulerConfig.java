package ang.test.schedulertestapp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
@ComponentScan(
        basePackages= "ang.test.schedulertestapp",
        basePackageClasses={SchedulerTestAppApplication.class})
public class SchedulerConfig {

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("scheduled-task");
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService scheduledThreadPool() {
        return Executors.newScheduledThreadPool(2);
    }

    @Bean(destroyMethod = "shutdown")
    public ExecutorService singleThreadExecutor(){
        return Executors.newSingleThreadExecutor();
    }
}