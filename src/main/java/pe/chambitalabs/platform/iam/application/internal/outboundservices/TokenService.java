package pe.chambitalabs.platform.iam.application.internal.outboundservices;

import pe.chambitalabs.platform.iam.domain.model.aggregates.User;
import pe.chambitalabs.platform.shared.infrastructure.security.AuthenticatedUser;

import java.util.Optional;

/** Port to the access token technology (JWT). */
public interface TokenService {

    String generateToken(User user);

    Optional<AuthenticatedUser> validateToken(String token);
}
