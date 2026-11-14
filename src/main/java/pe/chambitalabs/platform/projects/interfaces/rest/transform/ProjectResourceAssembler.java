package pe.chambitalabs.platform.projects.interfaces.rest.transform;

import pe.chambitalabs.platform.profiles.interfaces.acl.ProfilesContextFacade.StudentSnapshot;
import pe.chambitalabs.platform.projects.domain.model.aggregates.Project;
import pe.chambitalabs.platform.projects.domain.model.commands.ProjectCommands.PublishProjectCommand;
import pe.chambitalabs.platform.projects.domain.model.commands.ProjectCommands.SubmitProposalCommand;
import pe.chambitalabs.platform.projects.domain.model.entities.Proposal;
import pe.chambitalabs.platform.projects.domain.services.ProjectQueryService.StudentProposal;
import pe.chambitalabs.platform.projects.interfaces.rest.resources.CreateProjectResource;
import pe.chambitalabs.platform.projects.interfaces.rest.resources.CreateProposalResource;
import pe.chambitalabs.platform.projects.interfaces.rest.resources.ProjectResource;
import pe.chambitalabs.platform.projects.interfaces.rest.resources.ProposalResource;
import pe.chambitalabs.platform.projects.interfaces.rest.resources.StudentProposalResource;
import pe.chambitalabs.platform.projects.interfaces.rest.resources.StudentSummaryResource;

import java.util.List;
import java.util.UUID;

public final class ProjectResourceAssembler {

    private ProjectResourceAssembler() {
    }

    public static PublishProjectCommand toCommand(UUID clientId, CreateProjectResource resource) {
        return new PublishProjectCommand(clientId, resource.title(), resource.description(), resource.category(),
                resource.requiredSkills() == null ? List.of() : resource.requiredSkills(),
                resource.deliverables() == null ? List.of() : resource.deliverables(), resource.budget(),
                resource.deadline(), Boolean.TRUE.equals(resource.assisted()));
    }

    public static SubmitProposalCommand toCommand(UUID projectId, UUID studentId, CreateProposalResource resource) {
        return new SubmitProposalCommand(projectId, studentId, resource.price(), resource.deliveryDays(),
                resource.message());
    }

    public static ProjectResource toResource(Project project, String clientName) {
        return new ProjectResource(project.getId(), project.getClientId(), clientName, project.getTitle(),
                project.getDescription(), project.getCategory().name(), project.getRequiredSkills(),
                project.getDeliverables(), project.getBudget().amount(), project.getBudget().currency(),
                project.getDeadline(), project.isAssisted(), project.getStatus().name(), project.getProposals().size(),
                project.getCreatedAt());
    }

    public static ProposalResource toResource(UUID projectId, Proposal proposal, StudentSnapshot student) {
        StudentSummaryResource summary = student == null ? null : new StudentSummaryResource(student.userId(),
                student.fullName(), student.career(), student.cycle(), student.ratingAverage(), student.ratingCount(),
                student.ratingCount() == 0, student.certificateCount());
        return new ProposalResource(proposal.getId(), projectId, proposal.getStudentId(), proposal.getPrice().amount(),
                proposal.getPrice().currency(), proposal.getDeliveryDays(), proposal.getMessage(),
                proposal.getStatus().name(), proposal.getCreatedAt(), summary);
    }

    public static StudentProposalResource toResource(StudentProposal studentProposal) {
        var project = studentProposal.project();
        var proposal = studentProposal.proposal();
        return new StudentProposalResource(proposal.getId(), project.getId(), project.getTitle(),
                project.getStatus().name(), project.getCategory().name(), proposal.getPrice().amount(),
                proposal.getPrice().currency(), proposal.getDeliveryDays(), proposal.getStatus().name(),
                proposal.getCreatedAt());
    }
}
