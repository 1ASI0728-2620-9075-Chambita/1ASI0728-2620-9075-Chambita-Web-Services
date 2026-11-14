package pe.chambitalabs.platform.projects.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProposalResource(UUID id, UUID projectId, UUID studentId, BigDecimal price, String currency,
                               int deliveryDays, String message, String status, Instant createdAt,
                               StudentSummaryResource student) {
}
