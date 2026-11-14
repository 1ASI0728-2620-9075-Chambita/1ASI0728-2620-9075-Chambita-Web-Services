package pe.chambitalabs.platform.payments.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** The client paid; the money is held until the work is approved. Engagements activates the engagement. */
public record PaymentHeld(UUID paymentId, UUID engagementId, UUID clientId, UUID studentId, BigDecimal amount, Instant occurredOn) implements DomainEvent {
}
