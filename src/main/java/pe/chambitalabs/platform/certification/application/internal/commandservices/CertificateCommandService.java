package pe.chambitalabs.platform.certification.application.internal.commandservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import pe.chambitalabs.platform.certification.application.internal.outboundservices.BlockchainCertificateRegistry;
import pe.chambitalabs.platform.certification.application.internal.outboundservices.ExternalEngagementsService;
import pe.chambitalabs.platform.certification.domain.model.aggregates.Certificate;
import pe.chambitalabs.platform.certification.domain.model.aggregates.CertificateHolder;
import pe.chambitalabs.platform.certification.domain.model.commands.CertificateCommands.IssueCertificateCommand;
import pe.chambitalabs.platform.certification.domain.model.commands.CertificateCommands.RetryPendingCertificatesCommand;
import pe.chambitalabs.platform.certification.domain.model.commands.CertificateCommands.RevokeCertificateCommand;
import pe.chambitalabs.platform.certification.domain.model.valueobjects.CertificateStatus;
import pe.chambitalabs.platform.certification.infrastructure.persistence.jpa.repositories.CertificateHolderRepository;
import pe.chambitalabs.platform.certification.infrastructure.persistence.jpa.repositories.CertificateRepository;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Creates the certificate, sends the issuance transaction, confirms it and retries (TS11).
 * Each step runs in its own short transaction so no database connection waits for the network.
 */
@Service
public class CertificateCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateCommandService.class);

    private final CertificateRepository certificateRepository;
    private final CertificateHolderRepository holderRepository;
    private final BlockchainCertificateRegistry registry;
    private final ExternalEngagementsService externalEngagementsService;
    private final TransactionTemplate transactions;
    private final Clock clock;
    private final int maxAttempts;
    private final Duration confirmationTimeout;

    public CertificateCommandService(CertificateRepository certificateRepository,
                                     CertificateHolderRepository holderRepository,
                                     BlockchainCertificateRegistry registry,
                                     ExternalEngagementsService externalEngagementsService,
                                     TransactionTemplate transactions, Clock clock,
                                     @Value("${chambita.certification.max-attempts:3}") int maxAttempts,
                                     @Value("${chambita.certification.confirmation-timeout-seconds:90}")
                                     long confirmationTimeoutSeconds) {
        this.certificateRepository = certificateRepository;
        this.holderRepository = holderRepository;
        this.registry = registry;
        this.externalEngagementsService = externalEngagementsService;
        this.transactions = transactions;
        this.clock = clock;
        this.maxAttempts = maxAttempts;
        this.confirmationTimeout = Duration.ofSeconds(confirmationTimeoutSeconds);
    }

    /** Creates the certificate (no certificate when there is an open dispute) and tries to issue it. */
    public void handle(IssueCertificateCommand command) {
        if (externalEngagementsService.hasOpenDispute(command.engagementId())) {
            LOGGER.info("Engagement {} has an open dispute: no certificate is issued", command.engagementId());
            return;
        }
        var certificateId = transactions.execute(status -> {
            var existing = certificateRepository.findByEngagementId(command.engagementId());
            if (existing.isPresent()) {
                return existing.get().getId();
            }
            var holder = holderRepository.findByStudentId(command.studentId())
                    .orElseGet(() -> holderRepository.save(CertificateHolder.forStudent(command.studentId())));
            var certificate = Certificate.forCompletedEngagement(command.engagementId(), command.studentId(), holder,
                    command.projectTitle(), command.category(), command.score(), command.reviewedAt());
            return certificateRepository.save(certificate).getId();
        });
        submitAndConfirm(certificateId);
    }

    /** Confirms submitted certificates and resubmits the pending ones (daily job and manual retries). */
    public int handle(RetryPendingCertificatesCommand command) {
        var processed = 0;
        for (var certificate : certificateRepository.findByStatus(CertificateStatus.PENDING)) {
            try {
                if (certificate.getTransactionHash() != null) {
                    confirm(certificate.getId());
                } else {
                    submitAndConfirm(certificate.getId());
                }
                processed++;
            } catch (RuntimeException exception) {
                LOGGER.warn("Retry of certificate {} failed: {}", certificate.getId(), exception.getMessage());
            }
        }
        return processed;
    }

    public void handle(RevokeCertificateCommand command) {
        transactions.executeWithoutResult(status -> {
            var certificate = certificateRepository.findById(command.certificateId())
                    .orElseThrow(() -> new ResourceNotFoundException("certification.certificate.not-found"));
            certificate.revoke(Instant.now(clock));
            if (registry.isConfigured() && certificate.getTokenId() != null) {
                registry.revoke(certificate.getTokenId());
            }
            certificateRepository.save(certificate);
        });
    }

    /** Checks pending transactions of a student; used before listing the certificates. */
    public void refreshPendingOf(UUID studentId) {
        if (!registry.isConfigured()) {
            return;
        }
        certificateRepository.findByStudentIdOrderByIssuedAtDesc(studentId).stream()
                .filter(certificate -> certificate.getStatus() == CertificateStatus.PENDING
                        && certificate.getTransactionHash() != null)
                .forEach(certificate -> {
                    try {
                        confirm(certificate.getId());
                    } catch (RuntimeException exception) {
                        LOGGER.debug("Could not refresh certificate {}: {}", certificate.getId(), exception.getMessage());
                    }
                });
    }

    private void submitAndConfirm(UUID certificateId) {
        var submitted = transactions.execute(status -> {
            var certificate = certificateRepository.findById(certificateId).orElseThrow();
            if (!certificate.canRetry(maxAttempts)) {
                return false;
            }
            try {
                var transactionHash = registry.issue(certificate.getHolderAddress(), certificate.getCertificateHash());
                certificate.markSubmitted(transactionHash);
                certificateRepository.save(certificate);
                return true;
            } catch (RuntimeException exception) {
                LOGGER.warn("Issuance of certificate {} failed: {}", certificateId, exception.getMessage());
                if (registry.isConfigured()) {
                    certificate.countAttemptWithoutSubmission();
                }
                certificate.registerFailure(exception.getMessage(), maxAttempts, Instant.now(clock));
                certificateRepository.save(certificate);
                if (certificate.getStatus() == CertificateStatus.FAILED) {
                    LOGGER.error("ALERT: certificate {} FAILED after {} attempts", certificateId, maxAttempts);
                }
                return false;
            }
        });
        if (Boolean.TRUE.equals(submitted)) {
            waitForConfirmation(certificateId);
        }
    }

    private void waitForConfirmation(UUID certificateId) {
        var deadline = Instant.now().plus(confirmationTimeout);
        while (Instant.now().isBefore(deadline)) {
            if (confirm(certificateId)) {
                return;
            }
            try {
                Thread.sleep(3_000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        LOGGER.info("Certificate {} still pending confirmation; the daily job will confirm it", certificateId);
    }

    /** Returns true when the transaction was mined (issued or failed). */
    private boolean confirm(UUID certificateId) {
        return Boolean.TRUE.equals(transactions.execute(status -> {
            var certificate = certificateRepository.findById(certificateId).orElseThrow();
            if (certificate.getStatus() != CertificateStatus.PENDING || certificate.getTransactionHash() == null) {
                return true;
            }
            var issuance = registry.findIssuance(certificate.getTransactionHash());
            if (issuance.isEmpty()) {
                return false;
            }
            if (issuance.get().successful()) {
                certificate.markIssued(issuance.get().tokenId(), Instant.now(clock));
            } else {
                certificate.registerFailure(issuance.get().error(), maxAttempts, Instant.now(clock));
            }
            certificateRepository.save(certificate);
            return true;
        }));
    }
}
