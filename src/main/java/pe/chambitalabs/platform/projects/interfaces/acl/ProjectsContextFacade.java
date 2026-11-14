package pe.chambitalabs.platform.projects.interfaces.acl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.projects.infrastructure.persistence.jpa.repositories.ProjectRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Exposes the data of a project and a proposal, and the project owner, to other contexts. */
@Service
@Transactional(readOnly = true)
public class ProjectsContextFacade {

    private final ProjectRepository projectRepository;

    public ProjectsContextFacade(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Optional<ProposalSnapshot> fetchProposalSnapshot(UUID proposalId) {
        return projectRepository.findByProposalId(proposalId).flatMap(project -> project.findProposal(proposalId)
                .map(proposal -> new ProposalSnapshot(project.getId(), proposal.getId(), project.getClientId(),
                        proposal.getStudentId(), project.getTitle(), project.getDescription(),
                        project.getCategory().name(), project.getRequiredSkills(), project.getDeliverables(),
                        project.getBudget().amount(), project.getDeadline(), proposal.getPrice().amount(),
                        proposal.getDeliveryDays(), proposal.getMessage())));
    }

    public boolean isProjectOwner(UUID projectId, UUID userId) {
        return projectRepository.findById(projectId).map(project -> project.isOwnedBy(userId)).orElse(false);
    }

    /** Read-only view of a proposal and its project. */
    public record ProposalSnapshot(UUID projectId, UUID proposalId, UUID clientId, UUID studentId, String title,
                                   String description, String category, List<String> requiredSkills,
                                   List<String> deliverables, BigDecimal budget, LocalDate deadline, BigDecimal price,
                                   int deliveryDays, String message) {
    }
}
