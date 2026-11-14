package pe.chambitalabs.platform.engagements.application.internal.commandservices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.engagements.domain.model.aggregates.Engagement;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.ActivateEngagementCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.ApproveDeliverableCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.AutoApproveOverdueDeliverablesCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.CancelEngagementCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.CompleteEngagementCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.CreateEngagementCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.SubmitDeliverableCommand;
import pe.chambitalabs.platform.engagements.domain.model.valueobjects.EngagementStatus;
import pe.chambitalabs.platform.engagements.domain.services.EngagementCommandService;
import pe.chambitalabs.platform.engagements.infrastructure.persistence.jpa.repositories.EngagementRepository;
import pe.chambitalabs.platform.shared.domain.exceptions.ForbiddenException;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;
import pe.chambitalabs.platform.shared.domain.model.valueobjects.Money;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/** Applies the transitions of the engagement; the aggregate publishes its events (TS06). */
@Service
@Transactional
public class EngagementCommandServiceImpl implements EngagementCommandService {

    private final EngagementRepository repository;
    private final Clock clock;
    private final Duration reviewPeriod;

    public EngagementCommandServiceImpl(EngagementRepository repository, Clock clock,
                                        @Value("${chambita.engagements.auto-approval-days:7}") long autoApprovalDays) {
        this.repository = repository;
        this.clock = clock;
        this.reviewPeriod = Duration.ofDays(autoApprovalDays);
    }

    @Override
    public Engagement handle(CreateEngagementCommand command) {
        if (repository.existsByProposalId(command.proposalId())) {
            return null;
        }
        var engagement = Engagement.fromAcceptedProposal(command.projectId(), command.proposalId(),
                command.clientId(), command.studentId(), command.projectTitle(), command.category(),
                new Money(command.price(), command.currency()), command.deliveryDays(), command.deadline());
        return repository.save(engagement);
    }

    @Override
    public void handle(ActivateEngagementCommand command) {
        var engagement = find(command.engagementId());
        engagement.activate(Instant.now(clock));
        repository.save(engagement);
    }

    @Override
    public Engagement handle(SubmitDeliverableCommand command) {
        var engagement = find(command.engagementId());
        if (!engagement.getStudentId().equals(command.studentId())) {
            throw new ForbiddenException("engagements.student-required");
        }
        engagement.submitDeliverable(command.fileUrl(), command.linkUrl(), command.note(), Instant.now(clock));
        return repository.save(engagement);
    }

    @Override
    public Engagement handle(ApproveDeliverableCommand command) {
        var engagement = find(command.engagementId());
        if (!engagement.getClientId().equals(command.clientId())) {
            throw new ForbiddenException("engagements.client-required");
        }
        engagement.approveDeliverable(Instant.now(clock), false);
        return repository.saveAndFlush(engagement);
    }

    @Override
    public int handle(AutoApproveOverdueDeliverablesCommand command) {
        var now = Instant.now(clock);
        var approved = 0;
        for (var engagement : repository.findByStatus(EngagementStatus.IN_REVIEW)) {
            if (engagement.autoApproveIfOverdue(now, reviewPeriod)) {
                repository.save(engagement);
                approved++;
            }
        }
        return approved;
    }

    @Override
    public void handle(CompleteEngagementCommand command) {
        var engagement = find(command.engagementId());
        engagement.complete(Instant.now(clock));
        repository.save(engagement);
    }

    @Override
    public Engagement handle(CancelEngagementCommand command) {
        var engagement = find(command.engagementId());
        if (!engagement.getClientId().equals(command.clientId())) {
            throw new ForbiddenException("engagements.client-required");
        }
        engagement.cancel(Instant.now(clock));
        return repository.save(engagement);
    }

    private Engagement find(UUID engagementId) {
        return repository.findWithDeliverablesById(engagementId)
                .orElseThrow(() -> new ResourceNotFoundException("engagements.engagement.not-found"));
    }
}
