package pe.chambitalabs.platform.profiles.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.chambitalabs.platform.profiles.domain.model.aggregates.StudentProfile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {

    @EntityGraph(attributePaths = "portfolioItems")
    Optional<StudentProfile> findByUserId(UUID userId);

    @EntityGraph(attributePaths = "portfolioItems")
    List<StudentProfile> findByUserIdIn(Collection<UUID> userIds);

    boolean existsByUserId(UUID userId);
}
