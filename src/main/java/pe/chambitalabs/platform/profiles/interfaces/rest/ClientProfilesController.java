package pe.chambitalabs.platform.profiles.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.chambitalabs.platform.profiles.domain.model.commands.ClientProfileCommands.UpdateClientProfileCommand;
import pe.chambitalabs.platform.profiles.domain.model.queries.ProfileQueries.GetClientProfileByUserIdQuery;
import pe.chambitalabs.platform.profiles.domain.services.ClientProfileCommandService;
import pe.chambitalabs.platform.profiles.domain.services.ProfileQueryService;
import pe.chambitalabs.platform.profiles.interfaces.rest.resources.ClientProfileResource;
import pe.chambitalabs.platform.profiles.interfaces.rest.resources.UpdateClientProfileResource;
import pe.chambitalabs.platform.profiles.interfaces.rest.transform.ProfileResourceAssembler;
import pe.chambitalabs.platform.shared.domain.exceptions.ForbiddenException;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;
import pe.chambitalabs.platform.shared.infrastructure.security.AuthenticatedUser;

import java.util.UUID;

/** Public profile of clients. The client id is the user id. */
@RestController
@RequestMapping("/api/v1/clients/{clientId}")
@Tag(name = "Client profiles", description = "Public profile of clients")
public class ClientProfilesController {

    private final ClientProfileCommandService commandService;
    private final ProfileQueryService queryService;

    public ClientProfilesController(ClientProfileCommandService commandService, ProfileQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(summary = "Returns the client profile")
    public ClientProfileResource getProfile(@PathVariable UUID clientId) {
        return queryService.handle(new GetClientProfileByUserIdQuery(clientId))
                .map(ProfileResourceAssembler::toResource)
                .orElseThrow(() -> new ResourceNotFoundException("profiles.client.not-found"));
    }

    @PutMapping("/profile")
    @Operation(summary = "Updates the client profile")
    public ClientProfileResource updateProfile(@PathVariable UUID clientId,
                                               @Valid @RequestBody UpdateClientProfileResource resource,
                                               @AuthenticationPrincipal AuthenticatedUser principal) {
        if (!clientId.equals(principal.id())) {
            throw new ForbiddenException("profiles.owner-required");
        }
        return ProfileResourceAssembler.toResource(commandService.handle(new UpdateClientProfileCommand(clientId,
                resource.displayName(), resource.businessName(), resource.district())));
    }
}
