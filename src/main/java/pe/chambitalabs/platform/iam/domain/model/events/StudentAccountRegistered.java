package pe.chambitalabs.platform.iam.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/** A student created an account; IAM requests the verification code. */
public record StudentAccountRegistered(UUID userId, String email, String fullName, Instant occurredOn) implements DomainEvent {
}
