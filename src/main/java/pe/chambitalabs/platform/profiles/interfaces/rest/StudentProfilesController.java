package pe.chambitalabs.platform.profiles.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.AddPortfolioItemCommand;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.RemovePortfolioItemCommand;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.UpdateStudentProfileCommand;
import pe.chambitalabs.platform.profiles.domain.model.queries.ProfileQueries.GetStudentProfileByUserIdQuery;
import pe.chambitalabs.platform.profiles.domain.services.ProfileQueryService;
import pe.chambitalabs.platform.profiles.domain.services.StudentProfileCommandService;
import pe.chambitalabs.platform.profiles.interfaces.rest.resources.CreatePortfolioItemResource;
import pe.chambitalabs.platform.profiles.interfaces.rest.resources.PortfolioItemResource;
import pe.chambitalabs.platform.profiles.interfaces.rest.resources.StudentProfileResource;
import pe.chambitalabs.platform.profiles.interfaces.rest.resources.UpdateStudentProfileResource;
import pe.chambitalabs.platform.profiles.interfaces.rest.transform.ProfileResourceAssembler;
import pe.chambitalabs.platform.shared.domain.exceptions.ForbiddenException;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;
import pe.chambitalabs.platform.shared.infrastructure.security.AuthenticatedUser;

import java.util.UUID;

/** Student profile and portfolio (TS03, US13, US14, US15). The student id is the user id. */
@RestController
@RequestMapping("/api/v1/students/{studentId}")
@Tag(name = "Student profiles", description = "Professional profile and portfolio of students")
public class StudentProfilesController {

    private final StudentProfileCommandService commandService;
    private final ProfileQueryService queryService;

    public StudentProfilesController(StudentProfileCommandService commandService, ProfileQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @GetMapping
    @Operation(summary = "Returns the student profile with portfolio, average rating and certificates")
    public StudentProfileResource getProfile(@PathVariable UUID studentId) {
        return queryService.handle(new GetStudentProfileByUserIdQuery(studentId))
                .map(ProfileResourceAssembler::toResource)
                .orElseThrow(() -> new ResourceNotFoundException("profiles.student.not-found"));
    }

    @PutMapping("/profile")
    @Operation(summary = "Completes or updates the professional profile")
    public StudentProfileResource updateProfile(@PathVariable UUID studentId,
                                                @Valid @RequestBody UpdateStudentProfileResource resource,
                                                @AuthenticationPrincipal AuthenticatedUser principal) {
        requireOwner(studentId, principal);
        var profile = commandService.handle(new UpdateStudentProfileCommand(studentId, resource.career(),
                resource.cycle(), resource.bio(), resource.skills(), resource.serviceCategories()));
        return ProfileResourceAssembler.toResource(profile);
    }

    @PostMapping("/portfolio-items")
    @Operation(summary = "Adds a work to the portfolio")
    public ResponseEntity<PortfolioItemResource> addPortfolioItem(@PathVariable UUID studentId,
                                                                  @Valid @RequestBody CreatePortfolioItemResource resource,
                                                                  @AuthenticationPrincipal AuthenticatedUser principal) {
        requireOwner(studentId, principal);
        var item = commandService.handle(new AddPortfolioItemCommand(studentId, resource.title(),
                resource.description(), resource.imageUrl(), resource.externalUrl()));
        return ResponseEntity.status(HttpStatus.CREATED).body(ProfileResourceAssembler.toResource(item));
    }

    @DeleteMapping("/portfolio-items/{itemId}")
    @Operation(summary = "Removes a work from the portfolio")
    public ResponseEntity<Void> removePortfolioItem(@PathVariable UUID studentId, @PathVariable UUID itemId,
                                                    @AuthenticationPrincipal AuthenticatedUser principal) {
        requireOwner(studentId, principal);
        commandService.handle(new RemovePortfolioItemCommand(studentId, itemId));
        return ResponseEntity.noContent().build();
    }

    private static void requireOwner(UUID studentId, AuthenticatedUser principal) {
        if (!studentId.equals(principal.id())) {
            throw new ForbiddenException("profiles.owner-required");
        }
    }
}
