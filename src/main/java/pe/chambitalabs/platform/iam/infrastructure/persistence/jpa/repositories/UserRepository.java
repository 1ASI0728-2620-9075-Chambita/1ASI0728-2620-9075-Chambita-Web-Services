package pe.chambitalabs.platform.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.chambitalabs.platform.iam.domain.model.aggregates.User;
import pe.chambitalabs.platform.iam.domain.model.valueobjects.EmailAddress;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(EmailAddress email);

    boolean existsByEmail(EmailAddress email);
}
