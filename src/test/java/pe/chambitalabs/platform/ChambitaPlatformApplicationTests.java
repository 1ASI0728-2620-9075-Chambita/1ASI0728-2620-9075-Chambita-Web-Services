package pe.chambitalabs.platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import pe.chambitalabs.platform.support.EmbeddedPostgresConfiguration;

/** Boots the whole application: Flyway migrations and JPA mappings are validated against PostgreSQL. */
@SpringBootTest
@Import(EmbeddedPostgresConfiguration.class)
class ChambitaPlatformApplicationTests {

    @Test
    void contextLoads() {
    }
}
