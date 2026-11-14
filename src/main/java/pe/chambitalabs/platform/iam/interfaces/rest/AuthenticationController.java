package pe.chambitalabs.platform.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.chambitalabs.platform.iam.domain.services.UserCommandService;
import pe.chambitalabs.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import pe.chambitalabs.platform.iam.interfaces.rest.resources.SignInResource;
import pe.chambitalabs.platform.iam.interfaces.rest.resources.SignUpResource;
import pe.chambitalabs.platform.iam.interfaces.rest.resources.UserResource;
import pe.chambitalabs.platform.iam.interfaces.rest.transform.UserResourceAssembler;

/** Sign-up and sign-in (TS01). */
@RestController
@RequestMapping("/api/v1/authentication")
@Tag(name = "Authentication", description = "Sign-up and sign-in with JWT")
@SecurityRequirements
public class AuthenticationController {

    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    @PostMapping("/sign-up")
    @Operation(summary = "Registers a student or a client account")
    public ResponseEntity<UserResource> signUp(@Valid @RequestBody SignUpResource resource) {
        var user = userCommandService.handle(UserResourceAssembler.toCommand(resource));
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResourceAssembler.toResource(user));
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Signs in and returns an access token")
    public ResponseEntity<AuthenticatedUserResource> signIn(@Valid @RequestBody SignInResource resource) {
        var session = userCommandService.handle(UserResourceAssembler.toCommand(resource));
        return ResponseEntity.ok(UserResourceAssembler.toResource(session));
    }
}
