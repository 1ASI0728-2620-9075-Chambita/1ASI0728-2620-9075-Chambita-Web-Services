package pe.chambitalabs.platform.projects.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StudentProposalResource(UUID id, UUID projectId, String projectTitle, String projectStatus,
                                      String category, BigDecimal price, String currency, int deliveryDays,
                                      String status, Instant createdAt) {
}
