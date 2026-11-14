package pe.chambitalabs.platform.iam.interfaces.rest.transform;

import pe.chambitalabs.platform.iam.domain.model.aggregates.User;
import pe.chambitalabs.platform.iam.domain.model.commands.SignInCommand;
import pe.chambitalabs.platform.iam.domain.model.commands.SignUpCommand;
import pe.chambitalabs.platform.iam.domain.services.UserCommandService.AuthenticatedSession;
import pe.chambitalabs.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import pe.chambitalabs.platform.iam.interfaces.rest.resources.SignInResource;
import pe.chambitalabs.platform.iam.interfaces.rest.resources.SignUpResource;
import pe.chambitalabs.platform.iam.interfaces.rest.resources.UserResource;

/** Transforms IAM resources into commands and aggregates into resources. */
public final class UserResourceAssembler {

    private UserResourceAssembler() {
    }

    public static SignUpCommand toCommand(SignUpResource resource) {
        return new SignUpCommand(resource.email(), resource.password(), resource.fullName(), resource.role());
    }

    public static SignInCommand toCommand(SignInResource resource) {
        return new SignInCommand(resource.email(), resource.password());
    }

    public static UserResource toResource(User user) {
        return new UserResource(user.getId(), user.getEmail().value(), user.getFullName(), user.getRole().name(),
                user.getStatus().name(), user.isStudentVerified());
    }

    public static AuthenticatedUserResource toResource(AuthenticatedSession session) {
        var user = session.user();
        return new AuthenticatedUserResource(user.getId(), user.getEmail().value(), user.getFullName(),
                user.getRole().name(), user.isStudentVerified(), session.token());
    }
}
