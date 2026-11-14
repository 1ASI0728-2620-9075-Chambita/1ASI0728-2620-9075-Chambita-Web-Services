package pe.chambitalabs.platform.profiles.domain.services;

import pe.chambitalabs.platform.profiles.domain.model.aggregates.ClientProfile;
import pe.chambitalabs.platform.profiles.domain.model.commands.ClientProfileCommands.CreateClientProfileCommand;
import pe.chambitalabs.platform.profiles.domain.model.commands.ClientProfileCommands.UpdateClientProfileCommand;

public interface ClientProfileCommandService {

    ClientProfile handle(CreateClientProfileCommand command);

    ClientProfile handle(UpdateClientProfileCommand command);
}
