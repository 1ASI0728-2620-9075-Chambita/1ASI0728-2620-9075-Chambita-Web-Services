package pe.chambitalabs.platform.engagements.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EngagementResource(UUID id, UUID projectId, UUID proposalId, UUID clientId, UUID studentId,
                                 String projectTitle, String category, BigDecimal agreedPrice, String currency,
                                 int deliveryDays, LocalDate deadline, String status, Instant createdAt,
                                 Instant activatedAt, Instant completedAt, List<DeliverableResource> deliverables) {
}
