package pe.chambitalabs.platform.certification.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import pe.chambitalabs.platform.certification.domain.model.events.CertificateIssuanceFailed;
import pe.chambitalabs.platform.certification.domain.model.events.CertificateIssued;
import pe.chambitalabs.platform.certification.domain.model.valueobjects.CertificateStatus;
import pe.chambitalabs.platform.certification.domain.services.CertificateFingerprintService;
import pe.chambitalabs.platform.shared.domain.exceptions.ConflictException;
import pe.chambitalabs.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Experience certificate of a completed and reviewed engagement. Only its hash goes on-chain.
 * PENDING → ISSUED, or FAILED after the maximum number of attempts; ISSUED → REVOKED.
 */
@Entity
@Table(schema = "certification", name = "certificates")
public class Certificate extends AuditableAbstractAggregateRoot<Certificate> {

    @Column(name = "engagement_id", nullable = false, unique = true)
    private UUID engagementId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "holder_id", nullable = false)
    private UUID holderId;

    @Column(name = "holder_address", nullable = false, length = 42)
    private String holderAddress;

    @Column(name = "project_title", nullable = false, length = 120)
    private String projectTitle;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(nullable = false)
    private int score;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "certificate_hash", nullable = false, unique = true, length = 66)
    private String certificateHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CertificateStatus status;

    @Column(name = "token_id", precision = 78)
    private BigInteger tokenId;

    @Column(name = "transaction_hash", length = 66)
    private String transactionHash;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected Certificate() {
    }

    public static Certificate forCompletedEngagement(UUID engagementId, UUID studentId, CertificateHolder holder,
                                                     String projectTitle, String category, int score,
                                                     Instant issuedAt) {
        var certificate = new Certificate();
        certificate.engagementId = engagementId;
        certificate.studentId = studentId;
        certificate.holderId = holder.getHolderId();
        certificate.holderAddress = holder.getHolderAddress();
        certificate.projectTitle = projectTitle;
        certificate.category = category;
        certificate.score = score;
        certificate.issuedAt = issuedAt.truncatedTo(ChronoUnit.SECONDS);
        certificate.certificateHash = CertificateFingerprintService.fingerprint(certificate.getId(), holder.getHolderId(),
                projectTitle, category, score, certificate.issuedAt);
        certificate.status = CertificateStatus.PENDING;
        return certificate;
    }

    /** The issuance transaction was sent to the network. */
    public void markSubmitted(String transactionHash) {
        this.transactionHash = transactionHash;
        this.attempts++;
        this.lastError = null;
    }

    /** The transaction was confirmed: the certificate is issued. */
    public void markIssued(BigInteger tokenId, Instant now) {
        if (status == CertificateStatus.ISSUED) {
            return;
        }
        this.tokenId = tokenId;
        this.status = CertificateStatus.ISSUED;
        addDomainEvent(new CertificateIssued(getId(), studentId, engagementId, certificateHash, tokenId,
                transactionHash, now));
    }

    /** Registers a failed attempt; after the maximum number of attempts the certificate is FAILED. */
    public void registerFailure(String error, int maxAttempts, Instant now) {
        this.lastError = error == null ? "unknown error" : error.substring(0, Math.min(error.length(), 500));
        this.transactionHash = null;
        if (attempts >= maxAttempts) {
            status = CertificateStatus.FAILED;
            addDomainEvent(new CertificateIssuanceFailed(getId(), studentId, lastError, attempts, now));
        }
    }

    public void countAttemptWithoutSubmission() {
        attempts++;
    }

    public boolean canRetry(int maxAttempts) {
        return status == CertificateStatus.PENDING && attempts < maxAttempts;
    }

    public void revoke(Instant now) {
        if (status != CertificateStatus.ISSUED) {
            throw new ConflictException("certification.certificate.not-found");
        }
        status = CertificateStatus.REVOKED;
        revokedAt = now;
    }

    /** Public link of the verification portal for this certificate. */
    public String verificationUrl(String portalBaseUrl) {
        var base = portalBaseUrl.endsWith("/") ? portalBaseUrl : portalBaseUrl + "/";
        return base + "?hash=" + certificateHash;
    }

    public UUID getEngagementId() {
        return engagementId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getHolderId() {
        return holderId;
    }

    public String getHolderAddress() {
        return holderAddress;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public String getCategory() {
        return category;
    }

    public int getScore() {
        return score;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public String getCertificateHash() {
        return certificateHash;
    }

    public CertificateStatus getStatus() {
        return status;
    }

    public BigInteger getTokenId() {
        return tokenId;
    }

    public String getTransactionHash() {
        return transactionHash;
    }

    public int getAttempts() {
        return attempts;
    }

    public String getLastError() {
        return lastError;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }
}
