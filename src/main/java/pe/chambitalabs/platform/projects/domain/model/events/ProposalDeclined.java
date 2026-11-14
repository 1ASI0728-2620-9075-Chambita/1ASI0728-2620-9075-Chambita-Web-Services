package pe.chambitalabs.platform.projects.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** A proposal was declined by the client or closed because another one was accepted. */
public record ProposalDeclined(UUID projectId, UUID proposalId, UUID clientId, UUID studentId, String projectTitle, String reason, Instant occurredOn) implements DomainEvent {
}
