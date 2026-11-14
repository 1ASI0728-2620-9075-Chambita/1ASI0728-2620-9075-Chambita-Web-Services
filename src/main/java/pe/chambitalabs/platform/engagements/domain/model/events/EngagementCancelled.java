package pe.chambitalabs.platform.engagements.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** The client cancelled the engagement before paying. */
public record EngagementCancelled(UUID engagementId, UUID clientId, UUID studentId, Instant occurredOn) implements DomainEvent {
}
