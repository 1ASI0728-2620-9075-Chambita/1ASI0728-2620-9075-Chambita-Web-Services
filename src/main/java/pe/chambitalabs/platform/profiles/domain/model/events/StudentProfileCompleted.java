package pe.chambitalabs.platform.profiles.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** The student completed the profile with at least one service category. */
public record StudentProfileCompleted(UUID profileId, UUID userId, Instant occurredOn) implements DomainEvent {
}
