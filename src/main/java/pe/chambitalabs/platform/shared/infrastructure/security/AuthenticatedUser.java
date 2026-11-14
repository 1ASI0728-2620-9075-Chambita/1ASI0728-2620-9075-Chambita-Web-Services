package pe.chambitalabs.platform.shared.infrastructure.security;

import java.util.UUID;

/**
 * Identity carried by the JWT (IAM publishes it in the token; the other contexts conform to it).
 */
public record AuthenticatedUser(UUID id, String email, String role) {

    public static final String STUDENT = "STUDENT";
    public static final String CLIENT = "CLIENT";
    public static final String ADMIN = "ADMIN";

    public boolean isStudent() {
        return STUDENT.equals(role);
    }

    public boolean isClient() {
        return CLIENT.equals(role);
    }

    public boolean isAdmin() {
        return ADMIN.equals(role);
    }
}
