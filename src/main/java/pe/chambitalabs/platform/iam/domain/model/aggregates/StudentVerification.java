package pe.chambitalabs.platform.iam.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import pe.chambitalabs.platform.iam.domain.model.valueobjects.VerificationStatus;
import pe.chambitalabs.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/** Verification of a student's institutional email with a short-lived six-digit code. */
@Entity
@Table(schema = "iam", name = "student_verifications")
public class StudentVerification extends AuditableAbstractAggregateRoot<StudentVerification> {

    public static final Duration VALIDITY = Duration.ofMinutes(15);
    public static final int MAX_ATTEMPTS = 5;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "institutional_email", nullable = false, length = 160)
    private String institutionalEmail;

    @Column(name = "code_hash", nullable = false)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private int attempts;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus status;

    protected StudentVerification() {
    }

    public StudentVerification(UUID userId, String institutionalEmail, String codeHash, Instant now) {
        this.userId = userId;
        this.institutionalEmail = institutionalEmail;
        this.codeHash = codeHash;
        this.expiresAt = now.plus(VALIDITY);
        this.status = VerificationStatus.PENDING;
    }

    public boolean isExpired(Instant now) {
        return status == VerificationStatus.EXPIRED || now.isAfter(expiresAt) || attempts >= MAX_ATTEMPTS;
    }

    public void registerFailedAttempt() {
        attempts++;
        if (attempts >= MAX_ATTEMPTS) {
            status = VerificationStatus.EXPIRED;
        }
    }

    public void confirm(Instant now) {
        if (isExpired(now)) {
            status = VerificationStatus.EXPIRED;
            throw new IllegalStateException("Verification code expired");
        }
        status = VerificationStatus.VERIFIED;
    }

    public void expire() {
        status = VerificationStatus.EXPIRED;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getInstitutionalEmail() {
        return institutionalEmail;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public VerificationStatus getStatus() {
        return status;
    }
}
