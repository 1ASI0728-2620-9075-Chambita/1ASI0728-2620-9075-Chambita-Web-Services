package pe.chambitalabs.platform.reputation.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.chambitalabs.platform.reputation.domain.model.aggregates.ReviewableEngagement;

import java.util.Optional;
import java.util.UUID;

public interface ReviewableEngagementRepository extends JpaRepository<ReviewableEngagement, UUID> {

    Optional<ReviewableEngagement> findByEngagementId(UUID engagementId);
}
