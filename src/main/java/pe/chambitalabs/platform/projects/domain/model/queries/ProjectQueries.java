package pe.chambitalabs.platform.projects.domain.model.queries;

import pe.chambitalabs.platform.projects.domain.model.valueobjects.ProjectStatus;
import pe.chambitalabs.platform.projects.domain.model.valueobjects.ServiceCategory;

import java.math.BigDecimal;
import java.util.UUID;

/** Queries of the Projects context. */
public final class ProjectQueries {

    private ProjectQueries() {
    }

    public record GetProjectByIdQuery(UUID projectId) {
    }

    public record SearchProjectsQuery(ServiceCategory category, BigDecimal minBudget, BigDecimal maxBudget,
                                      ProjectStatus status, String text) {
    }

    public record GetProjectsByClientIdQuery(UUID clientId, ProjectStatus status) {
    }

    public record GetProposalsByProjectIdQuery(UUID projectId, UUID requesterId, String sort) {
    }

    public record GetProposalsByStudentIdQuery(UUID studentId) {
    }
}
