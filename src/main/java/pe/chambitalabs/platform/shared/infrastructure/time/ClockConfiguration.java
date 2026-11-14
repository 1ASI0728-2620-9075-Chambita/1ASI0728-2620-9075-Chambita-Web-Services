package pe.chambitalabs.platform.shared.infrastructure.time;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/** Business clock in Lima time; tests can replace it. */
@Configuration
public class ClockConfiguration {

    public static final ZoneId LIMA = ZoneId.of("America/Lima");

    @Bean
    @ConditionalOnMissingBean
    public Clock clock() {
        return Clock.system(LIMA);
    }
}
