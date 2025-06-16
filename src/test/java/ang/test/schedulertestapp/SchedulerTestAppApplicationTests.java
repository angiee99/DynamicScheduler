package ang.test.schedulertestapp;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootTest
@EnableJpaRepositories(basePackages = "ang.test.schedulertestapp.persistence")
public class SchedulerTestAppApplicationTests {

    @Test
    public void contextLoads() {
    }

}
