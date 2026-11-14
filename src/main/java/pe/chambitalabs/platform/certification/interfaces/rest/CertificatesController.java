package pe.chambitalabs.platform.certification.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.chambitalabs.platform.certification.application.internal.commandservices.CertificateCommandService;
import pe.chambitalabs.platform.certification.application.internal.outboundservices.BlockchainCertificateRegistry;
import pe.chambitalabs.platform.certification.domain.model.aggregates.Certificate;
import pe.chambitalabs.platform.certification.domain.model.valueobjects.CertificateStatus;
import pe.chambitalabs.platform.certification.domain.model.commands.CertificateCommands.RevokeCertificateCommand;
import pe.chambitalabs.platform.certification.infrastructure.persistence.jpa.repositories.CertificateRepository;
import pe.chambitalabs.platform.certification.interfaces.rest.resources.CertificateResource;
import pe.chambitalabs.platform.certification.interfaces.rest.resources.PublicCertificateResource;
import pe.chambitalabs.platform.shared.domain.exceptions.ForbiddenException;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;
import pe.chambitalabs.platform.shared.infrastructure.security.AuthenticatedUser;

import java.util.List;
import java.util.UUID;

/** Certificates of the student, public certificate data and revocation (US33, US34, US36). */
@RestController
@Tag(name = "Certificates", description = "Experience certificates registered on Ethereum Sepolia")
public class CertificatesController {

    private final CertificateRepository repository;
    private final CertificateCommandService commandService;
    private final BlockchainCertificateRegistry registry;
    private final String portalBaseUrl;

    public CertificatesController(CertificateRepository repository, CertificateCommandService commandService,
                                  BlockchainCertificateRegistry registry,
                                  @Value("${chambita.certification.portal-base-url}") String portalBaseUrl) {
        this.repository = repository;
        this.commandService = commandService;
        this.registry = registry;
        this.portalBaseUrl = portalBaseUrl;
    }

    @GetMapping("/api/v1/students/{studentId}/certificates")
    @Operation(summary = "Lists the certificates of a student: all of them for the holder, only issued ones for others")
    public List<CertificateResource> byStudent(@PathVariable UUID studentId,
                                               @AuthenticationPrincipal AuthenticatedUser principal) {
        if (studentId.equals(principal.id())) {
            commandService.refreshPendingOf(studentId);
            return repository.findByStudentIdOrderByIssuedAtDesc(studentId).stream().map(this::toResource).toList();
        }
        // Clients deciding whether to hire the student only see issued certificates (US15).
        return repository.findByStudentIdOrderByIssuedAtDesc(studentId).stream()
                .filter(certificate -> certificate.getStatus() == CertificateStatus.ISSUED)
                .map(this::toResource).toList();
    }

    @GetMapping("/api/v1/certificates/{certificateId}")
    @Operation(summary = "Returns a certificate (holder or admin)")
    public CertificateResource byId(@PathVariable UUID certificateId,
                                    @AuthenticationPrincipal AuthenticatedUser principal) {
        var certificate = repository.findById(certificateId)
                .orElseThrow(() -> new ResourceNotFoundException("certification.certificate.not-found"));
        if (!certificate.getStudentId().equals(principal.id()) && !principal.isAdmin()) {
            throw new ForbiddenException("certification.owner-required");
        }
        return toResource(certificate);
    }

    @PostMapping("/api/v1/certificates/{certificateId}/revocation")
    @Operation(summary = "Revokes a certificate for proven fraud (admin only)")
    public ResponseEntity<Void> revoke(@PathVariable UUID certificateId,
                                       @AuthenticationPrincipal AuthenticatedUser principal) {
        if (!principal.isAdmin()) {
            throw new ForbiddenException("error.forbidden");
        }
        commandService.handle(new RevokeCertificateCommand(certificateId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/public/certificates/{certificateHash}")
    @SecurityRequirements
    @Operation(summary = "Public data of a certificate by its hash, used by the verification portal")
    public PublicCertificateResource publicByHash(@PathVariable String certificateHash) {
        var certificate = repository.findByCertificateHash(certificateHash.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("certification.certificate.not-found"));
        return new PublicCertificateResource(certificate.getCertificateHash(), certificate.getProjectTitle(),
                certificate.getCategory(), certificate.getScore(), certificate.getIssuedAt(),
                certificate.getStatus().name(), certificate.getTokenId(), certificate.getTransactionHash(),
                certificate.getRevokedAt(), registry.contractAddress());
    }

    private CertificateResource toResource(Certificate certificate) {
        return new CertificateResource(certificate.getId(), certificate.getEngagementId(), certificate.getStudentId(),
                certificate.getProjectTitle(), certificate.getCategory(), certificate.getScore(),
                certificate.getIssuedAt(), certificate.getCertificateHash(), certificate.getStatus().name(),
                certificate.getTokenId(), certificate.getTransactionHash(), certificate.getRevokedAt(),
                certificate.verificationUrl(portalBaseUrl), registry.contractAddress());
    }
}
