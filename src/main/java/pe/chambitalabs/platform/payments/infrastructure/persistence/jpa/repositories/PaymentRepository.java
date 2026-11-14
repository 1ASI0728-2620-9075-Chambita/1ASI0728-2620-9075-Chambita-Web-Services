package pe.chambitalabs.platform.payments.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.chambitalabs.platform.payments.domain.model.aggregates.Payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByEngagementId(UUID engagementId);

    List<Payment> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
}
