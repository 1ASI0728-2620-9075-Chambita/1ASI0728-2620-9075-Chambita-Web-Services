package pe.chambitalabs.platform.projects.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.chambitalabs.platform.projects.application.internal.outboundservices.ExternalProfilesService;
import pe.chambitalabs.platform.projects.domain.model.commands.ProjectCommands.AcceptProposalCommand;
import pe.chambitalabs.platform.projects.domain.model.commands.ProjectCommands.DeclineProposalCommand;
import pe.chambitalabs.platform.projects.domain.model.entities.Proposal;
import pe.chambitalabs.platform.projects.domain.model.queries.ProjectQueries.GetProposalsByProjectIdQuery;
import pe.chambitalabs.platform.projects.domain.model.queries.ProjectQueries.GetProposalsByStudentIdQuery;
import pe.chambitalabs.platform.projects.domain.services.ProjectCommandService;
import pe.chambitalabs.platform.projects.domain.services.ProjectQueryService;
import pe.chambitalabs.platform.projects.interfaces.rest.resources.CreateProposalResource;
import pe.chambitalabs.platform.projects.interfaces.rest.resources.ProposalResource;
import pe.chambitalabs.platform.projects.interfaces.rest.resources.StudentProposalResource;
import pe.chambitalabs.platform.projects.interfaces.rest.transform.ProjectResourceAssembler;
import pe.chambitalabs.platform.shared.domain.exceptions.ForbiddenException;
import pe.chambitalabs.platform.shared.infrastructure.security.AuthenticatedUser;

import java.util.List;
import java.util.UUID;

/** Submission, listing, acceptance and decline of proposals (TS05, US19, US20, US21). */
@RestController
@Tag(name = "Proposals", description = "Proposals of students to projects")
public class ProposalsController {

    private final ProjectCommandService commandService;
    private final ProjectQueryService queryService;
    private final ExternalProfilesService externalProfilesService;

    public ProposalsController(ProjectCommandService commandService, ProjectQueryService queryService,
                               ExternalProfilesService externalProfilesService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.externalProfilesService = externalProfilesService;
    }

    @PostMapping("/api/v1/projects/{projectId}/proposals")
    @Operation(summary = "Sends a proposal to an open project (verified students with a complete profile)")
    public ResponseEntity<ProposalResource> submit(@PathVariable UUID projectId,
                                                   @Valid @RequestBody CreateProposalResource resource,
                                                   @AuthenticationPrincipal AuthenticatedUser principal) {
        if (!principal.isStudent()) {
            throw new ForbiddenException("projects.student-required");
        }
        var proposal = commandService.handle(ProjectResourceAssembler.toCommand(projectId, principal.id(), resource));
        return ResponseEntity.status(HttpStatus.CREATED).body(withStudent(projectId, proposal));
    }

    @GetMapping("/api/v1/projects/{projectId}/proposals")
    @Operation(summary = "Lists the proposals of a project; sort by rating, price or deliveryDays")
    public List<ProposalResource> list(@PathVariable UUID projectId, @RequestParam(required = false) String sort,
                                       @AuthenticationPrincipal AuthenticatedUser principal) {
        var proposals = queryService.handle(new GetProposalsByProjectIdQuery(projectId, principal.id(), sort));
        var students = externalProfilesService.fetchStudents(proposals.stream().map(Proposal::getStudentId).toList());
        return proposals.stream().map(proposal ->
                ProjectResourceAssembler.toResource(projectId, proposal, students.get(proposal.getStudentId()))).toList();
    }

    @PostMapping("/api/v1/projects/{projectId}/proposals/{proposalId}/acceptance")
    @Operation(summary = "Accepts a proposal; the engagement is created pending payment")
    public ProposalResource accept(@PathVariable UUID projectId, @PathVariable UUID proposalId,
                                   @AuthenticationPrincipal AuthenticatedUser principal) {
        var proposal = commandService.handle(new AcceptProposalCommand(projectId, proposalId, principal.id()));
        return withStudent(projectId, proposal);
    }

    @PostMapping("/api/v1/projects/{projectId}/proposals/{proposalId}/decline")
    @Operation(summary = "Declines a proposal")
    public ProposalResource decline(@PathVariable UUID projectId, @PathVariable UUID proposalId,
                                    @AuthenticationPrincipal AuthenticatedUser principal) {
        var proposal = commandService.handle(new DeclineProposalCommand(projectId, proposalId, principal.id()));
        return withStudent(projectId, proposal);
    }

    @GetMapping("/api/v1/students/{studentId}/proposals")
    @Operation(summary = "Lists the proposals sent by the authenticated student")
    public List<StudentProposalResource> byStudent(@PathVariable UUID studentId,
                                                   @AuthenticationPrincipal AuthenticatedUser principal) {
        if (!studentId.equals(principal.id())) {
            throw new ForbiddenException("projects.student-required");
        }
        return queryService.handle(new GetProposalsByStudentIdQuery(studentId)).stream()
                .map(ProjectResourceAssembler::toResource).toList();
    }

    private ProposalResource withStudent(UUID projectId, Proposal proposal) {
        var students = externalProfilesService.fetchStudents(List.of(proposal.getStudentId()));
        return ProjectResourceAssembler.toResource(projectId, proposal, students.get(proposal.getStudentId()));
    }
}
