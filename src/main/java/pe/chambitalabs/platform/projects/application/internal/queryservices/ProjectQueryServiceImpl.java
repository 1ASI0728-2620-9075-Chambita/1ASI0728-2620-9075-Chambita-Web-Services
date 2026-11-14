package pe.chambitalabs.platform.projects.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.projects.application.internal.outboundservices.ExternalProfilesService;
import pe.chambitalabs.platform.projects.domain.model.aggregates.Project;
import pe.chambitalabs.platform.projects.domain.model.entities.Proposal;
import pe.chambitalabs.platform.projects.domain.model.queries.ProjectQueries.GetProjectByIdQuery;
import pe.chambitalabs.platform.projects.domain.model.queries.ProjectQueries.GetProjectsByClientIdQuery;
import pe.chambitalabs.platform.projects.domain.model.queries.ProjectQueries.GetProposalsByProjectIdQuery;
import pe.chambitalabs.platform.projects.domain.model.queries.ProjectQueries.GetProposalsByStudentIdQuery;
import pe.chambitalabs.platform.projects.domain.model.queries.ProjectQueries.SearchProjectsQuery;
import pe.chambitalabs.platform.projects.domain.model.valueobjects.ProjectStatus;
import pe.chambitalabs.platform.projects.domain.services.ProjectQueryService;
import pe.chambitalabs.platform.projects.infrastructure.persistence.jpa.repositories.ProjectRepository;
import pe.chambitalabs.platform.shared.domain.exceptions.ForbiddenException;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Searches projects and sorts proposals with the ratings that Profiles exposes. */
@Service
@Transactional(readOnly = true)
public class ProjectQueryServiceImpl implements ProjectQueryService {

    private final ProjectRepository projectRepository;
    private final ExternalProfilesService externalProfilesService;

    public ProjectQueryServiceImpl(ProjectRepository projectRepository,
                                   ExternalProfilesService externalProfilesService) {
        this.projectRepository = projectRepository;
        this.externalProfilesService = externalProfilesService;
    }

    @Override
    public Optional<Project> handle(GetProjectByIdQuery query) {
        return projectRepository.findWithProposalsById(query.projectId());
    }

    @Override
    public List<Project> handle(SearchProjectsQuery query) {
        var status = query.status() == null ? ProjectStatus.OPEN : query.status();
        var text = query.text() == null ? "" : query.text().trim().toLowerCase(Locale.ROOT);
        return projectRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .filter(project -> query.category() == null || project.getCategory() == query.category())
                .filter(project -> query.minBudget() == null || atLeast(project.getBudget().amount(), query.minBudget()))
                .filter(project -> query.maxBudget() == null || atLeast(query.maxBudget(), project.getBudget().amount()))
                .filter(project -> text.isEmpty() || project.getTitle().toLowerCase(Locale.ROOT).contains(text)
                        || project.getDescription().toLowerCase(Locale.ROOT).contains(text))
                .toList();
    }

    @Override
    public List<Project> handle(GetProjectsByClientIdQuery query) {
        return projectRepository.findByClientIdOrderByCreatedAtDesc(query.clientId()).stream()
                .filter(project -> query.status() == null || project.getStatus() == query.status())
                .toList();
    }

    @Override
    public List<Proposal> handle(GetProposalsByProjectIdQuery query) {
        var project = projectRepository.findWithProposalsById(query.projectId())
                .orElseThrow(() -> new ResourceNotFoundException("projects.project.not-found"));
        if (!project.isOwnedBy(query.requesterId())) {
            throw new ForbiddenException("projects.owner-required");
        }
        var proposals = project.getProposals();
        var sort = query.sort() == null ? "" : query.sort();
        return switch (sort) {
            case "price" -> proposals.stream().sorted(Comparator.comparing(p -> p.getPrice().amount())).toList();
            case "deliveryDays" -> proposals.stream().sorted(Comparator.comparingInt(Proposal::getDeliveryDays)).toList();
            case "rating" -> {
                var students = externalProfilesService.fetchStudents(
                        proposals.stream().map(Proposal::getStudentId).toList());
                yield proposals.stream().sorted(Comparator.comparing((Proposal p) -> {
                    var snapshot = students.get(p.getStudentId());
                    return snapshot == null ? BigDecimal.ZERO : snapshot.ratingAverage();
                }).reversed()).toList();
            }
            default -> proposals;
        };
    }

    @Override
    public List<StudentProposal> handle(GetProposalsByStudentIdQuery query) {
        return projectRepository.findWithProposalsOfStudent(query.studentId()).stream()
                .flatMap(project -> project.getProposals().stream()
                        .filter(proposal -> proposal.getStudentId().equals(query.studentId()))
                        .map(proposal -> new StudentProposal(project, proposal)))
                .sorted(Comparator.comparing((StudentProposal sp) -> sp.proposal().getCreatedAt()).reversed())
                .toList();
    }

    private static boolean atLeast(BigDecimal value, BigDecimal minimum) {
        return value.compareTo(minimum) >= 0;
    }
}
