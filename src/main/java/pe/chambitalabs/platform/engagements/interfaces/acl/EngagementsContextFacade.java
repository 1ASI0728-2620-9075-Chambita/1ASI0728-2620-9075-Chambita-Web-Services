package pe.chambitalabs.platform.engagements.interfaces.acl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.engagements.domain.model.valueobjects.EngagementStatus;
import pe.chambitalabs.platform.engagements.infrastructure.persistence.jpa.repositories.EngagementRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Exposes to Payments and Certification the summary of an engagement. */
@Service
@Transactional(readOnly = true)
public class EngagementsContextFacade {

    private final EngagementRepository repository;

    public EngagementsContextFacade(EngagementRepository repository) {
        this.repository = repository;
    }

    public Optional<EngagementSummary> fetchEngagementSummary(UUID engagementId) {
        return repository.findById(engagementId).map(engagement -> new EngagementSummary(engagement.getId(),
                engagement.getClientId(), engagement.getStudentId(), engagement.getProjectTitle(),
                engagement.getCategory(), engagement.getStatus().name(), engagement.getCompletedAt()));
    }

    public boolean isParticipant(UUID engagementId, UUID userId) {
        return repository.findById(engagementId).map(engagement -> engagement.isParticipant(userId)).orElse(false);
    }

    public boolean hasOpenDispute(UUID engagementId) {
        return repository.findById(engagementId)
                .map(engagement -> engagement.getStatus() == EngagementStatus.DISPUTED).orElse(false);
    }

    /** Read-only view of an engagement. */
    public record EngagementSummary(UUID engagementId, UUID clientId, UUID studentId, String projectTitle,
                                    String category, String status, Instant completedAt) {
    }
}
