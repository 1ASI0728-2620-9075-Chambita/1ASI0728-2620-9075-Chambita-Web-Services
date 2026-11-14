package pe.chambitalabs.platform.profiles.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.profiles.domain.model.aggregates.ClientProfile;
import pe.chambitalabs.platform.profiles.domain.model.commands.ClientProfileCommands.CreateClientProfileCommand;
import pe.chambitalabs.platform.profiles.domain.model.commands.ClientProfileCommands.UpdateClientProfileCommand;
import pe.chambitalabs.platform.profiles.domain.services.ClientProfileCommandService;
import pe.chambitalabs.platform.profiles.infrastructure.persistence.jpa.repositories.ClientProfileRepository;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;

/** Creates and updates client profiles. */
@Service
@Transactional
public class ClientProfileCommandServiceImpl implements ClientProfileCommandService {

    private final ClientProfileRepository repository;

    public ClientProfileCommandServiceImpl(ClientProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    public ClientProfile handle(CreateClientProfileCommand command) {
        return repository.findByUserId(command.userId())
                .orElseGet(() -> repository.save(new ClientProfile(command.userId(), command.displayName())));
    }

    @Override
    public ClientProfile handle(UpdateClientProfileCommand command) {
        var profile = repository.findByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("profiles.client.not-found"));
        profile.update(command.displayName(), command.businessName(), command.district());
        return repository.save(profile);
    }
}
