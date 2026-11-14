package pe.chambitalabs.platform.profiles.domain.model.commands;

import java.util.UUID;

/** Commands of the client profile. */
public final class ClientProfileCommands {

    private ClientProfileCommands() {
    }

    public record CreateClientProfileCommand(UUID userId, String displayName) {
    }

    public record UpdateClientProfileCommand(UUID userId, String displayName, String businessName, String district) {
    }
}
