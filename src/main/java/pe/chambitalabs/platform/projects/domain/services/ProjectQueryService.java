package pe.chambitalabs.platform.projects.domain.services;

import pe.chambitalabs.platform.projects.domain.model.aggregates.Project;
import pe.chambitalabs.platform.projects.domain.model.entities.Proposal;
import pe.chambitalabs.platform.projects.domain.model.queries.ProjectQueries.GetProjectByIdQuery;
import pe.chambitalabs.platform.projects.domain.model.queries.ProjectQueries.GetProjectsByClientIdQuery;
import pe.chambitalabs.platform.projects.domain.model.queries.ProjectQueries.GetProposalsByProjectIdQuery;
import pe.chambitalabs.platform.projects.domain.model.queries.ProjectQueries.GetProposalsByStudentIdQuery;
import pe.chambitalabs.platform.projects.domain.model.queries.ProjectQueries.SearchProjectsQuery;

import java.util.List;
import java.util.Optional;

public interface ProjectQueryService {

    Optional<Project> handle(GetProjectByIdQuery query);

    List<Project> handle(SearchProjectsQuery query);

    List<Project> handle(GetProjectsByClientIdQuery query);

    List<Proposal> handle(GetProposalsByProjectIdQuery query);

    List<StudentProposal> handle(GetProposalsByStudentIdQuery query);

    /** A proposal of a student together with its project. */
    record StudentProposal(Project project, Proposal proposal) {
    }
}
