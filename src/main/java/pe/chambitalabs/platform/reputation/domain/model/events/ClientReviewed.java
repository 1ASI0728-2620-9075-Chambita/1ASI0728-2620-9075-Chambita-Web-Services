package pe.chambitalabs.platform.reputation.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** The student rated the client. */
public record ClientReviewed(UUID engagementId, UUID clientId, UUID studentId, int score, Instant occurredOn) implements DomainEvent {
}
