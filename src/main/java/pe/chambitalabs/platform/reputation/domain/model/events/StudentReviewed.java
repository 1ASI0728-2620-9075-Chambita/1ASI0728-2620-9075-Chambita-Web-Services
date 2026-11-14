package pe.chambitalabs.platform.reputation.domain.model.events;

import pe.chambitalabs.platform.shared.domain.model.events.DomainEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** The client rated the student; Certification issues the experience certificate. */
public record StudentReviewed(UUID engagementId, UUID studentId, UUID clientId, int score, String projectTitle, String category, Instant occurredOn) implements DomainEvent {
}
