package pe.chambitalabs.platform.profiles.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;

public record ClientProfileResource(UUID id, UUID userId, String displayName, String businessName, String district,
                                    BigDecimal ratingAverage, int ratingCount) {
}
