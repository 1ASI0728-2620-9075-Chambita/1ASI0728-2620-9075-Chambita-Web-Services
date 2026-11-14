package pe.chambitalabs.platform.reputation.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.chambitalabs.platform.reputation.domain.model.aggregates.Review;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByRevieweeIdOrderByCreatedAtDesc(UUID revieweeId);

    List<Review> findByEngagementId(UUID engagementId);
}
