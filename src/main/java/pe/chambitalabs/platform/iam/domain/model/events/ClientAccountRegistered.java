package pe.chambitalabs.platform.iam.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** A client created an account; Profiles creates the client profile. */
public record ClientAccountRegistered(UUID userId, String email, String fullName, Instant occurredOn) implements DomainEvent {
}
