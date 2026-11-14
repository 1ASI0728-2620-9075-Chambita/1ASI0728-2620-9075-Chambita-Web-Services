package pe.chambitalabs.platform.engagements.domain.model.commands;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Commands of the Engagements context. */
public final class EngagementCommands {

    private EngagementCommands() {
    }

    public record CreateEngagementCommand(UUID projectId, UUID proposalId, UUID clientId, UUID studentId,
                                          String projectTitle, String category, BigDecimal price, String currency,
                                          int deliveryDays, LocalDate deadline) {
    }

    public record ActivateEngagementCommand(UUID engagementId) {
    }

    public record SubmitDeliverableCommand(UUID engagementId, UUID studentId, String fileUrl, String linkUrl,
                                           String note) {
    }

    public record ApproveDeliverableCommand(UUID engagementId, UUID clientId) {
    }

    public record AutoApproveOverdueDeliverablesCommand() {
    }

    public record CompleteEngagementCommand(UUID engagementId) {
    }

    public record CancelEngagementCommand(UUID engagementId, UUID clientId) {
    }
}
