package pe.chambitalabs.platform.engagements.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** The student submitted the work; the engagement is in review. */
public record DeliverableSubmitted(UUID engagementId, UUID deliverableId, UUID clientId, UUID studentId, Instant occurredOn) implements DomainEvent {
}
