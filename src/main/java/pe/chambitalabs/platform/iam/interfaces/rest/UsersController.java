package pe.chambitalabs.platform.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.chambitalabs.platform.iam.domain.model.queries.GetUserByIdQuery;
import pe.chambitalabs.platform.iam.domain.services.UserQueryService;
import pe.chambitalabs.platform.iam.interfaces.rest.resources.UserResource;
import pe.chambitalabs.platform.iam.interfaces.rest.transform.UserResourceAssembler;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;
import pe.chambitalabs.platform.shared.infrastructure.security.AuthenticatedUser;

/** Data of the authenticated user. */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Authenticated user")
public class UsersController {

    private final UserQueryService userQueryService;

    public UsersController(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping("/me")
    @Operation(summary = "Returns the authenticated user")
    public UserResource me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return userQueryService.handle(new GetUserByIdQuery(principal.id()))
                .map(UserResourceAssembler::toResource)
                .orElseThrow(() -> new ResourceNotFoundException("iam.user.not-found"));
    }
}
