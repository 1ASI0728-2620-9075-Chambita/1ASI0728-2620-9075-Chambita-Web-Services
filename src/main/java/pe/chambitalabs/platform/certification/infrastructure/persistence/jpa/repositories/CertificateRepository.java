package pe.chambitalabs.platform.certification.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.chambitalabs.platform.certification.domain.model.aggregates.Certificate;
import pe.chambitalabs.platform.certification.domain.model.valueobjects.CertificateStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {

    List<Certificate> findByStudentIdOrderByIssuedAtDesc(UUID studentId);

    Optional<Certificate> findByEngagementId(UUID engagementId);

    Optional<Certificate> findByCertificateHash(String certificateHash);

    List<Certificate> findByStatus(CertificateStatus status);
}
