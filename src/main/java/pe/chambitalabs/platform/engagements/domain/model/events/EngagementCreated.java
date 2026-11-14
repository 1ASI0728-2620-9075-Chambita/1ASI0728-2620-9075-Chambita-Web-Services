package pe.chambitalabs.platform.engagements.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** The engagement was created pending payment; Payments creates the pending payment. */
public record EngagementCreated(UUID engagementId, UUID projectId, UUID clientId, UUID studentId, String projectTitle, BigDecimal amount, String currency, Instant occurredOn) implements DomainEvent {
}
