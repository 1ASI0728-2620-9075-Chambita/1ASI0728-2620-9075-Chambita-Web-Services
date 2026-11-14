package pe.chambitalabs.platform.projects.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.chambitalabs.platform.projects.application.internal.outboundservices.ExternalProfilesService;
import pe.chambitalabs.platform.projects.domain.model.queries.ProjectQueries.GetProjectByIdQuery;
import pe.chambitalabs.platform.projects.domain.model.queries.ProjectQueries.GetProjectsByClientIdQuery;
import pe.chambitalabs.platform.projects.domain.model.queries.ProjectQueries.SearchProjectsQuery;
import pe.chambitalabs.platform.projects.domain.model.valueobjects.ProjectStatus;
import pe.chambitalabs.platform.projects.domain.model.valueobjects.ServiceCategory;
import pe.chambitalabs.platform.projects.domain.services.ProjectCommandService;
import pe.chambitalabs.platform.projects.domain.services.ProjectQueryService;
import pe.chambitalabs.platform.projects.interfaces.rest.resources.CreateProjectResource;
import pe.chambitalabs.platform.projects.interfaces.rest.resources.ProjectResource;
import pe.chambitalabs.platform.projects.interfaces.rest.transform.ProjectResourceAssembler;
import pe.chambitalabs.platform.shared.domain.exceptions.ForbiddenException;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;
import pe.chambitalabs.platform.shared.infrastructure.security.AuthenticatedUser;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/** Publication, search and detail of projects (TS04, US16, US18, US24). */
@RestController
@Tag(name = "Projects", description = "Publication and search of projects")
public class ProjectsController {

    private final ProjectCommandService commandService;
    private final ProjectQueryService queryService;
    private final ExternalProfilesService externalProfilesService;

    public ProjectsController(ProjectCommandService commandService, ProjectQueryService queryService,
                              ExternalProfilesService externalProfilesService) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.externalProfilesService = externalProfilesService;
    }

    @PostMapping("/api/v1/projects")
    @Operation(summary = "Publishes a project (clients only)")
    public ResponseEntity<ProjectResource> publish(@Valid @RequestBody CreateProjectResource resource,
                                                   @AuthenticationPrincipal AuthenticatedUser principal) {
        if (!principal.isClient()) {
            throw new ForbiddenException("projects.client-required");
        }
        var project = commandService.handle(ProjectResourceAssembler.toCommand(principal.id(), resource));
        var body = ProjectResourceAssembler.toResource(project, externalProfilesService.fetchClientName(principal.id()));
        return ResponseEntity.created(URI.create("/api/v1/projects/" + project.getId()))
                .body(body);
    }

    @GetMapping("/api/v1/projects")
    @Operation(summary = "Searches projects by category, budget range, status (OPEN by default) and text")
    public List<ProjectResource> search(@RequestParam(required = false) ServiceCategory category,
                                        @RequestParam(required = false) BigDecimal minBudget,
                                        @RequestParam(required = false) BigDecimal maxBudget,
                                        @RequestParam(required = false) ProjectStatus status,
                                        @RequestParam(required = false, name = "q") String text) {
        return queryService.handle(new SearchProjectsQuery(category, minBudget, maxBudget, status, text)).stream()
                .map(project -> ProjectResourceAssembler.toResource(project,
                        externalProfilesService.fetchClientName(project.getClientId())))
                .toList();
    }

    @GetMapping("/api/v1/projects/{projectId}")
    @Operation(summary = "Returns a project")
    public ProjectResource getById(@PathVariable UUID projectId) {
        return queryService.handle(new GetProjectByIdQuery(projectId))
                .map(project -> ProjectResourceAssembler.toResource(project,
                        externalProfilesService.fetchClientName(project.getClientId())))
                .orElseThrow(() -> new ResourceNotFoundException("projects.project.not-found"));
    }

    @GetMapping("/api/v1/clients/{clientId}/projects")
    @Operation(summary = "Lists the projects of the authenticated client, optionally filtered by status")
    public List<ProjectResource> byClient(@PathVariable UUID clientId,
                                          @RequestParam(required = false) ProjectStatus status,
                                          @AuthenticationPrincipal AuthenticatedUser principal) {
        if (!clientId.equals(principal.id())) {
            throw new ForbiddenException("projects.owner-required");
        }
        var clientName = externalProfilesService.fetchClientName(clientId);
        return queryService.handle(new GetProjectsByClientIdQuery(clientId, status)).stream()
                .map(project -> ProjectResourceAssembler.toResource(project, clientName)).toList();
    }

}
