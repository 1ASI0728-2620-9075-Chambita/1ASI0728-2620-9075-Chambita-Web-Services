package pe.chambitalabs.platform.engagements.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.engagements.domain.model.aggregates.Engagement;
import pe.chambitalabs.platform.engagements.domain.model.queries.EngagementQueries.GetEngagementByIdQuery;
import pe.chambitalabs.platform.engagements.domain.model.queries.EngagementQueries.GetEngagementsByParticipantQuery;
import pe.chambitalabs.platform.engagements.domain.services.EngagementQueryService;
import pe.chambitalabs.platform.engagements.infrastructure.persistence.jpa.repositories.EngagementRepository;
import pe.chambitalabs.platform.shared.domain.exceptions.ForbiddenException;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class EngagementQueryServiceImpl implements EngagementQueryService {

    private final EngagementRepository repository;

    public EngagementQueryServiceImpl(EngagementRepository repository) {
        this.repository = repository;
    }

    @Override
    public Engagement handle(GetEngagementByIdQuery query) {
        var engagement = repository.findWithDeliverablesById(query.engagementId())
                .orElseThrow(() -> new ResourceNotFoundException("engagements.engagement.not-found"));
        if (!engagement.isParticipant(query.requesterId())) {
            throw new ForbiddenException("engagements.participant-required");
        }
        return engagement;
    }

    @Override
    public List<Engagement> handle(GetEngagementsByParticipantQuery query) {
        return repository.findByParticipant(query.participantId()).stream()
                .filter(engagement -> query.status() == null || engagement.getStatus() == query.status())
                .toList();
    }
}
