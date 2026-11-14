package pe.chambitalabs.platform.projects.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ProjectResource(UUID id, UUID clientId, String clientName, String title, String description,
                              String category, List<String> requiredSkills, List<String> deliverables,
                              BigDecimal budget, String currency, LocalDate deadline, boolean assisted, String status,
                              int proposalsCount, Instant createdAt) {
}
