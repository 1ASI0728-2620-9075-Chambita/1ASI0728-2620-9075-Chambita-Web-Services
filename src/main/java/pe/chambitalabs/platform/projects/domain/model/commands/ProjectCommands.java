package pe.chambitalabs.platform.projects.domain.model.commands;

import pe.chambitalabs.platform.projects.domain.model.valueobjects.ServiceCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Commands of the Projects context. */
public final class ProjectCommands {

    private ProjectCommands() {
    }

    public record PublishProjectCommand(UUID clientId, String title, String description, ServiceCategory category,
                                        List<String> requiredSkills, List<String> deliverables, BigDecimal budget,
                                        LocalDate deadline, boolean assisted) {
    }

    public record SubmitProposalCommand(UUID projectId, UUID studentId, BigDecimal price, int deliveryDays,
                                        String message) {
    }

    public record AcceptProposalCommand(UUID projectId, UUID proposalId, UUID clientId) {
    }

    public record DeclineProposalCommand(UUID projectId, UUID proposalId, UUID clientId) {
    }
}
