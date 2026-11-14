package pe.chambitalabs.platform.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.chambitalabs.platform.iam.domain.model.aggregates.StudentVerification;
import pe.chambitalabs.platform.iam.domain.model.valueobjects.VerificationStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentVerificationRepository extends JpaRepository<StudentVerification, UUID> {

    Optional<StudentVerification> findFirstByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, VerificationStatus status);

    List<StudentVerification> findByUserIdAndStatus(UUID userId, VerificationStatus status);
}
