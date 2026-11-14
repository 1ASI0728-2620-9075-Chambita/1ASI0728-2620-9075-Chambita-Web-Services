package pe.chambitalabs.platform.reputation.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** The accumulated rating of a user changed; Profiles updates its copy. */
public record RatingUpdated(UUID userId, BigDecimal average, int count, Instant occurredOn) implements DomainEvent {
}
