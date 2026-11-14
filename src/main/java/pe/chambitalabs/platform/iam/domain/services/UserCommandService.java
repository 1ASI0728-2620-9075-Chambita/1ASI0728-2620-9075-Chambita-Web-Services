package pe.chambitalabs.platform.iam.domain.services;

import pe.chambitalabs.platform.iam.domain.model.aggregates.User;
import pe.chambitalabs.platform.iam.domain.model.commands.SignInCommand;
import pe.chambitalabs.platform.iam.domain.model.commands.SignUpCommand;

/** Use cases of user accounts. */
public interface UserCommandService {

    User handle(SignUpCommand command);

    AuthenticatedSession handle(SignInCommand command);

    record AuthenticatedSession(User user, String token) {
    }
}
