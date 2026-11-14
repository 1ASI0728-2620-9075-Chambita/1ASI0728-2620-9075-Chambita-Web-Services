package pe.chambitalabs.platform;

import org.springframework.boot.SpringApplication;
import pe.chambitalabs.platform.support.EmbeddedPostgresConfiguration;

/**
 * Runs the API locally with an embedded PostgreSQL and the test secrets: {@code ./mvnw spring-boot:test-run}.
 * Verification codes are written to the log and certificates stay PENDING (no blockchain variables).
 */
public class TestChambitaPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.from(ChambitaPlatformApplication::main).with(EmbeddedPostgresConfiguration.class).run(args);
    }
}
