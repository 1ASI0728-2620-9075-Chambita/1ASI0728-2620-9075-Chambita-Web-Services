package pe.chambitalabs.platform.reputation.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;

public record ReputationResource(UUID userId, BigDecimal averageScore, int reviewCount) {
}
