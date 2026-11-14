package pe.chambitalabs.platform.iam.domain;

import org.junit.jupiter.api.Test;
import pe.chambitalabs.platform.iam.domain.model.aggregates.StudentVerification;
import pe.chambitalabs.platform.iam.domain.model.valueobjects.EmailAddress;
import pe.chambitalabs.platform.iam.domain.model.valueobjects.VerificationStatus;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StudentVerificationTest {

    private final Instant now = Instant.parse("2026-11-14T15:00:00Z");

    @Test
    void confirmsWithinTheValidityPeriod() {
        var verification = new StudentVerification(UUID.randomUUID(), "valeria@upc.edu.pe", "hash", now);
        verification.confirm(now.plusSeconds(60));
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.VERIFIED);
    }

    @Test
    void expiresAfterFifteenMinutes() {
        var verification = new StudentVerification(UUID.randomUUID(), "valeria@upc.edu.pe", "hash", now);
        assertThat(verification.isExpired(now.plus(StudentVerification.VALIDITY).plusSeconds(1))).isTrue();
        assertThatThrownBy(() -> verification.confirm(now.plus(StudentVerification.VALIDITY).plusSeconds(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void expiresAfterTooManyFailedAttempts() {
        var verification = new StudentVerification(UUID.randomUUID(), "valeria@upc.edu.pe", "hash", now);
        for (var attempt = 0; attempt < StudentVerification.MAX_ATTEMPTS; attempt++) {
            verification.registerFailedAttempt();
        }
        assertThat(verification.getStatus()).isEqualTo(VerificationStatus.EXPIRED);
    }

    @Test
    void normalizesEmailsAndExtractsTheDomain() {
        var email = new EmailAddress("  Valeria.Rojas@UPC.edu.pe ");
        assertThat(email.value()).isEqualTo("valeria.rojas@upc.edu.pe");
        assertThat(email.domain()).isEqualTo("upc.edu.pe");
        assertThatThrownBy(() -> new EmailAddress("not-an-email")).isInstanceOf(IllegalArgumentException.class);
    }
}
