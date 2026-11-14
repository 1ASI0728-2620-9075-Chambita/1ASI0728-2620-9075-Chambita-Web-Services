package pe.chambitalabs.platform.iam.interfaces.rest.resources;

import java.util.UUID;

public record AuthenticatedUserResource(UUID id, String email, String fullName, String role, boolean studentVerified,
                                        String token) {
}
