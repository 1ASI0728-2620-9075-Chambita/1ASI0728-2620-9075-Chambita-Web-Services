package pe.chambitalabs.platform.engagements.interfaces.rest;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.ApproveDeliverableCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.CancelEngagementCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.SubmitDeliverableCommand;
import pe.chambitalabs.platform.engagements.domain.model.queries.EngagementQueries.GetEngagementByIdQuery;
import pe.chambitalabs.platform.engagements.domain.model.queries.EngagementQueries.GetEngagementsByParticipantQuery;
import pe.chambitalabs.platform.engagements.domain.model.valueobjects.EngagementStatus;
import pe.chambitalabs.platform.engagements.domain.services.EngagementCommandService;
import pe.chambitalabs.platform.engagements.domain.services.EngagementQueryService;
import pe.chambitalabs.platform.engagements.interfaces.rest.resources.CreateDeliverableResource;
import pe.chambitalabs.platform.engagements.interfaces.rest.resources.EngagementResource;
import pe.chambitalabs.platform.engagements.interfaces.rest.transform.EngagementResourceAssembler;
import pe.chambitalabs.platform.shared.infrastructure.security.AuthenticatedUser;

import java.util.List;
import java.util.UUID;

/** Life cycle of the engagement: deliverables, approval and cancellation (TS06, US25, US27, US28). */
@RestController
@RequestMapping("/api/v1/engagements")
@Tag(name = "Engagements", description = "Engagements between clients and students")
public class EngagementsController {

    private final EngagementCommandService commandService;
    private final EngagementQueryService queryService;

    public EngagementsController(EngagementCommandService commandService, EngagementQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(summary = "Lists the engagements of the authenticated user, optionally filtered by status")
    public List<EngagementResource> list(@RequestParam(required = false) EngagementStatus status,
                                         @AuthenticationPrincipal AuthenticatedUser principal) {
        return queryService.handle(new GetEngagementsByParticipantQuery(principal.id(), status)).stream()
                .map(EngagementResourceAssembler::toResource).toList();
    }

    @GetMapping("/{engagementId}")
    @Operation(summary = "Returns an engagement (participants only)")
    public EngagementResource getById(@PathVariable UUID engagementId,
                                      @AuthenticationPrincipal AuthenticatedUser principal) {
        return EngagementResourceAssembler.toResource(
                queryService.handle(new GetEngagementByIdQuery(engagementId, principal.id())));
    }

    @PostMapping("/{engagementId}/deliverables")
    @Operation(summary = "Submits the final work with a file or a link; the engagement goes to IN_REVIEW")
    public ResponseEntity<EngagementResource> submitDeliverable(@PathVariable UUID engagementId,
                                                                @Valid @RequestBody CreateDeliverableResource resource,
                                                                @AuthenticationPrincipal AuthenticatedUser principal) {
        var engagement = commandService.handle(new SubmitDeliverableCommand(engagementId, principal.id(),
                resource.fileUrl(), resource.linkUrl(), resource.note()));
        return ResponseEntity.status(HttpStatus.CREATED).body(EngagementResourceAssembler.toResource(engagement));
    }

    @PostMapping("/{engagementId}/approval")
    @Operation(summary = "Approves the delivered work; Payments releases the payment")
    public EngagementResource approve(@PathVariable UUID engagementId,
                                      @AuthenticationPrincipal AuthenticatedUser principal) {
        return EngagementResourceAssembler.toResource(
                commandService.handle(new ApproveDeliverableCommand(engagementId, principal.id())));
    }

    @PostMapping("/{engagementId}/cancellation")
    @Operation(summary = "Cancels an engagement that is still pending payment")
    public EngagementResource cancel(@PathVariable UUID engagementId,
                                     @AuthenticationPrincipal AuthenticatedUser principal) {
        return EngagementResourceAssembler.toResource(
                commandService.handle(new CancelEngagementCommand(engagementId, principal.id())));
    }
}
