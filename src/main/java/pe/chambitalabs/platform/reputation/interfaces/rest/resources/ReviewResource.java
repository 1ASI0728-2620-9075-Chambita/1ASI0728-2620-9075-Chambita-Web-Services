package pe.chambitalabs.platform.reputation.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record ReviewResource(UUID id, UUID engagementId, UUID reviewerId, UUID revieweeId, String reviewerRole,
                             int score, String comment, Instant createdAt) {
}
