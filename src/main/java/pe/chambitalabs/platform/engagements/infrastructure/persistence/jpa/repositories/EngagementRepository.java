package pe.chambitalabs.platform.engagements.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.chambitalabs.platform.engagements.domain.model.aggregates.Engagement;
import pe.chambitalabs.platform.engagements.domain.model.valueobjects.EngagementStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EngagementRepository extends JpaRepository<Engagement, UUID> {

    @EntityGraph(attributePaths = "deliverables")
    Optional<Engagement> findWithDeliverablesById(UUID id);

    boolean existsByProposalId(UUID proposalId);

    @EntityGraph(attributePaths = "deliverables")
    @Query("select e from Engagement e where e.clientId = :userId or e.studentId = :userId order by e.createdAt desc")
    List<Engagement> findByParticipant(@Param("userId") UUID userId);

    @EntityGraph(attributePaths = "deliverables")
    List<Engagement> findByStatus(EngagementStatus status);
}
