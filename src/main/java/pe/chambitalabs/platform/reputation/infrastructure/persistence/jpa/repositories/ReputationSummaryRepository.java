package pe.chambitalabs.platform.reputation.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.chambitalabs.platform.reputation.domain.model.aggregates.ReputationSummary;

import java.util.Optional;
import java.util.UUID;

public interface ReputationSummaryRepository extends JpaRepository<ReputationSummary, UUID> {

    Optional<ReputationSummary> findByUserId(UUID userId);
}
