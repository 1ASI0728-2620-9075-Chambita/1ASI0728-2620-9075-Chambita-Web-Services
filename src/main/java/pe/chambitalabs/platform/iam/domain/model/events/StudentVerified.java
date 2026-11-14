package pe.chambitalabs.platform.iam.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** A student confirmed the institutional email; Profiles creates the student profile. */
public record StudentVerified(UUID userId, String email, String fullName, String university, Instant occurredOn) implements DomainEvent {
}
