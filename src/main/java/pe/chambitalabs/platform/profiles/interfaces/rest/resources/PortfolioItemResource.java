package pe.chambitalabs.platform.profiles.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record PortfolioItemResource(UUID id, String title, String description, String imageUrl, String externalUrl,
                                    Instant createdAt) {
}
