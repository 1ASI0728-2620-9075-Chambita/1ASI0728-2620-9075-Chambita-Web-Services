package pe.chambitalabs.platform.projects.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** The client accepted a proposal; Engagements creates the engagement. */
public record ProposalAccepted(UUID projectId, UUID proposalId, UUID clientId, UUID studentId, String projectTitle, String category, BigDecimal price, String currency, int deliveryDays, LocalDate deadline, Instant occurredOn) implements DomainEvent {
}
