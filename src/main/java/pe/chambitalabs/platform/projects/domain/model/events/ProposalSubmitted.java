package pe.chambitalabs.platform.projects.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** A student sent a proposal to a project. */
public record ProposalSubmitted(UUID projectId, UUID proposalId, UUID clientId, UUID studentId, String projectTitle, Instant occurredOn) implements DomainEvent {
}
