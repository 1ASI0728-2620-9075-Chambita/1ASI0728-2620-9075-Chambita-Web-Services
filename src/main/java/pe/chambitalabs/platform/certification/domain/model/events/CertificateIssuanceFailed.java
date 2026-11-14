package pe.chambitalabs.platform.certification.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

/** The issuance failed after the maximum number of attempts; an alert is logged for the team. */
public record CertificateIssuanceFailed(UUID certificateId, UUID studentId, String lastError, int attempts, Instant occurredOn) implements DomainEvent {
}
