package pe.chambitalabs.platform.payments.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** The payment was released to the student. Engagements completes the engagement. */
public record PaymentReleased(UUID paymentId, UUID engagementId, UUID clientId, UUID studentId, BigDecimal studentPayout, Instant occurredOn) implements DomainEvent {
}
