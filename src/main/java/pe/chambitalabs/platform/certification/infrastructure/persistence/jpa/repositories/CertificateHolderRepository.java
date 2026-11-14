package pe.chambitalabs.platform.certification.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.chambitalabs.platform.certification.domain.model.aggregates.CertificateHolder;

import java.util.Optional;
import java.util.UUID;

public interface CertificateHolderRepository extends JpaRepository<CertificateHolder, UUID> {

    Optional<CertificateHolder> findByStudentId(UUID studentId);
}
