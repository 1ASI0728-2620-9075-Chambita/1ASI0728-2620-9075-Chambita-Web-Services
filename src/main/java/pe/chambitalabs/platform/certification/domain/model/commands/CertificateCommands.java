package pe.chambitalabs.platform.certification.domain.model.commands;

import java.time.Instant;
import java.util.UUID;

/** Commands of the Certification context. */
public final class CertificateCommands {

    private CertificateCommands() {
    }

    public record IssueCertificateCommand(UUID engagementId, UUID studentId, String projectTitle, String category,
                                          int score, Instant reviewedAt) {
    }

    public record RetryPendingCertificatesCommand() {
    }

    public record RevokeCertificateCommand(UUID certificateId) {
    }
}
