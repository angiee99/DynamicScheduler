package ang.test.schedulertestapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "ang.test.schedulertestapp.persistence")
public class SchedulerTestAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchedulerTestAppApplication.class, args);
    }

}
