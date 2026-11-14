package pe.chambitalabs.platform.support;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Starts a real PostgreSQL server (embedded binaries, no Docker) shared by every test context.
 */
@TestConfiguration(proxyBeanMethods = false)
public class EmbeddedPostgresConfiguration {

    private static EmbeddedPostgres postgres;

    static synchronized EmbeddedPostgres server() {
        if (postgres == null) {
            try {
                postgres = EmbeddedPostgres.builder().start();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        postgres.close();
                    } catch (IOException ignored) {
                        // shutting down
                    }
                }));
            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }
        return postgres;
    }

    @Bean
    @Primary
    public DataSource dataSource() {
        return server().getPostgresDatabase();
    }
}
