package pe.chambitalabs.platform.projects.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.projects.application.internal.outboundservices.ExternalProfilesService;
import pe.chambitalabs.platform.projects.domain.model.aggregates.Project;
import pe.chambitalabs.platform.projects.domain.model.commands.ProjectCommands.AcceptProposalCommand;
import pe.chambitalabs.platform.projects.domain.model.commands.ProjectCommands.DeclineProposalCommand;
import pe.chambitalabs.platform.projects.domain.model.commands.ProjectCommands.PublishProjectCommand;
import pe.chambitalabs.platform.projects.domain.model.commands.ProjectCommands.SubmitProposalCommand;
import pe.chambitalabs.platform.projects.domain.model.entities.Proposal;
import pe.chambitalabs.platform.projects.domain.services.ProjectCommandService;
import pe.chambitalabs.platform.projects.infrastructure.persistence.jpa.repositories.ProjectRepository;
import pe.chambitalabs.platform.shared.domain.exceptions.BusinessRuleException;
import pe.chambitalabs.platform.shared.domain.exceptions.ForbiddenException;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;
import pe.chambitalabs.platform.shared.domain.model.valueobjects.Money;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

/** Publishes projects and manages proposals; the aggregate publishes its events (TS04, TS05). */
@Service
@Transactional
public class ProjectCommandServiceImpl implements ProjectCommandService {

    private final ProjectRepository projectRepository;
    private final ExternalProfilesService externalProfilesService;
    private final Clock clock;

    public ProjectCommandServiceImpl(ProjectRepository projectRepository,
                                     ExternalProfilesService externalProfilesService, Clock clock) {
        this.projectRepository = projectRepository;
        this.externalProfilesService = externalProfilesService;
        this.clock = clock;
    }

    @Override
    public Project handle(PublishProjectCommand command) {
        if (!command.deadline().isAfter(LocalDate.now(clock))) {
            throw new BusinessRuleException("projects.deadline-in-past");
        }
        var project = Project.publish(command.clientId(), command.title(), command.description(), command.category(),
                command.requiredSkills(), command.deliverables(), Money.soles(command.budget()), command.deadline(),
                command.assisted());
        return projectRepository.save(project);
    }

    @Override
    public Proposal handle(SubmitProposalCommand command) {
        var project = find(command.projectId());
        if (!externalProfilesService.isStudentEligible(command.studentId())) {
            throw new BusinessRuleException("projects.student-not-eligible");
        }
        var proposal = project.submitProposal(command.studentId(), Money.soles(command.price()),
                command.deliveryDays(), command.message());
        projectRepository.save(project);
        return proposal;
    }

    @Override
    public Proposal handle(AcceptProposalCommand command) {
        var project = ownedProject(command.projectId(), command.clientId());
        var proposal = project.acceptProposal(command.proposalId(), clock);
        projectRepository.save(project);
        return proposal;
    }

    @Override
    public Proposal handle(DeclineProposalCommand command) {
        var project = ownedProject(command.projectId(), command.clientId());
        var proposal = project.declineProposal(command.proposalId());
        projectRepository.save(project);
        return proposal;
    }

    private Project ownedProject(UUID projectId, UUID clientId) {
        var project = find(projectId);
        if (!project.isOwnedBy(clientId)) {
            throw new ForbiddenException("projects.owner-required");
        }
        return project;
    }

    private Project find(UUID projectId) {
        return projectRepository.findWithProposalsById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("projects.project.not-found"));
    }
}
