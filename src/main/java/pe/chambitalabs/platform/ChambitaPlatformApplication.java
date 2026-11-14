package pe.chambitalabs.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Chambita RESTful API: a modular monolith with one Spring Modulith module per bounded context
 * (IAM, Profiles, Projects, Engagements, Payments, Reputation, Certification).
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class ChambitaPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChambitaPlatformApplication.class, args);
    }
}
