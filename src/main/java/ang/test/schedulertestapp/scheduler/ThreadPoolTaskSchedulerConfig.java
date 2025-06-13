package ang.test.schedulertestapp.scheduler;

import ang.test.schedulertestapp.SchedulerTestAppApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@ComponentScan(
        basePackages= "ang.test.schedulertestapp",
        basePackageClasses={SchedulerTestAppApplication.class})
public class ThreadPoolTaskSchedulerConfig {

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler threadPoolTaskScheduler(){
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5);
        threadPoolTaskScheduler.setThreadNamePrefix("scheduled-task");
        threadPoolTaskScheduler.initialize();
        return threadPoolTaskScheduler;
    }
}