package pe.chambitalabs.platform.profiles.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.chambitalabs.platform.profiles.domain.model.aggregates.ClientProfile;

import java.util.Optional;
import java.util.UUID;

public interface ClientProfileRepository extends JpaRepository<ClientProfile, UUID> {

    Optional<ClientProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
