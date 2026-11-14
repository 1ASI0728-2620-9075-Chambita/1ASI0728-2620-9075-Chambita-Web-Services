package pe.chambitalabs.platform.projects.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.chambitalabs.platform.projects.domain.model.aggregates.Project;
import pe.chambitalabs.platform.projects.domain.model.valueobjects.ProjectStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @EntityGraph(attributePaths = "proposals")
    Optional<Project> findWithProposalsById(UUID id);

    @EntityGraph(attributePaths = "proposals")
    List<Project> findByStatusOrderByCreatedAtDesc(ProjectStatus status);

    @EntityGraph(attributePaths = "proposals")
    List<Project> findByClientIdOrderByCreatedAtDesc(UUID clientId);

    @EntityGraph(attributePaths = "proposals")
    @Query("select distinct p from Project p join p.proposals pr where pr.studentId = :studentId")
    List<Project> findWithProposalsOfStudent(@Param("studentId") UUID studentId);

    @EntityGraph(attributePaths = "proposals")
    @Query("select p from Project p join p.proposals pr where pr.id = :proposalId")
    Optional<Project> findByProposalId(@Param("proposalId") UUID proposalId);
}
