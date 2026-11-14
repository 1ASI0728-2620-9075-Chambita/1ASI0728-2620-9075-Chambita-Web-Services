package pe.chambitalabs.platform.shared.domain.model.events;

import java.time.Instant;

/**
 * Common contract of the domain events exchanged between bounded contexts.
 */
public interface DomainEvent {

    Instant occurredOn();
}
