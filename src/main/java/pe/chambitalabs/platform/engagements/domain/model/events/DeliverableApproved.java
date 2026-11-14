package pe.chambitalabs.platform.engagements.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** The client (or the 7-day rule) approved the work; Payments releases the payment. */
public record DeliverableApproved(UUID engagementId, UUID deliverableId, UUID clientId, UUID studentId, boolean automatic, Instant occurredOn) implements DomainEvent {
}
