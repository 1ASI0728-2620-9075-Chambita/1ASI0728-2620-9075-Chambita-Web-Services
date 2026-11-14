package pe.chambitalabs.platform.engagements.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** The payment was released and the engagement is completed; Reputation enables the reviews. */
public record EngagementCompleted(UUID engagementId, UUID clientId, UUID studentId, String projectTitle, String category, Instant occurredOn) implements DomainEvent {
}
