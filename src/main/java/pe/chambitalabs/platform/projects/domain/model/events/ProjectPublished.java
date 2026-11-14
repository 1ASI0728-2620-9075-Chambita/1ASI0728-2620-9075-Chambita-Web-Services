package pe.chambitalabs.platform.projects.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** A client published a project. */
public record ProjectPublished(UUID projectId, UUID clientId, String title, String category, BigDecimal budget, Instant occurredOn) implements DomainEvent {
}
