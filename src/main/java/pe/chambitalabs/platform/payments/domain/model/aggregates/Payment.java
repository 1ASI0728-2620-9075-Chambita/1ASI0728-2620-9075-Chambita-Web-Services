package pe.chambitalabs.platform.payments.domain.model.aggregates;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import pe.chambitalabs.platform.payments.domain.model.events.PaymentHeld;
import pe.chambitalabs.platform.payments.domain.model.events.PaymentReleased;
import pe.chambitalabs.platform.payments.domain.model.valueobjects.PaymentStatus;
import pe.chambitalabs.platform.payments.domain.services.ServiceFeePolicy;
import pe.chambitalabs.platform.shared.domain.exceptions.ConflictException;
import pe.chambitalabs.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import pe.chambitalabs.platform.shared.domain.model.valueobjects.Money;

import java.time.Instant;
import java.util.UUID;

/**
 * Protected (simulated) payment of an engagement. State machine:
 * PENDING → HELD → RELEASED; released only once (QA05). The client pays the agreed amount
 * plus the service fee; the student receives the agreed amount.
 */
@Entity
@Table(schema = "payments", name = "payments")
public class Payment extends AuditableAbstractAggregateRoot<Payment> {

    @Column(name = "engagement_id", nullable = false, unique = true)
    private UUID engagementId;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "currency", nullable = false))})
    private Money amount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "service_fee_amount", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "service_fee_currency", nullable = false))})
    private Money serviceFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "held_at")
    private Instant heldAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    @Version
    private long version;

    protected Payment() {
    }

    public static Payment pending(UUID engagementId, UUID clientId, UUID studentId, Money amount,
                                  ServiceFeePolicy feePolicy) {
        var payment = new Payment();
        payment.engagementId = engagementId;
        payment.clientId = clientId;
        payment.studentId = studentId;
        payment.amount = amount;
        payment.serviceFee = feePolicy.calculateFee(amount);
        payment.status = PaymentStatus.PENDING;
        return payment;
    }

    /** The client confirms the simulated payment: the money is held. */
    public void confirm(Instant now) {
        if (status != PaymentStatus.PENDING) {
            throw new ConflictException("payments.invalid-status", status.name());
        }
        status = PaymentStatus.HELD;
        heldAt = now;
        addDomainEvent(new PaymentHeld(getId(), engagementId, clientId, studentId, amount.amount(), now));
    }

    /** Releases the payment; a repeated release (duplicated event) does not change it. */
    public boolean release(Instant now) {
        if (status == PaymentStatus.RELEASED) {
            return false;
        }
        if (status != PaymentStatus.HELD) {
            throw new ConflictException("payments.invalid-status", status.name());
        }
        status = PaymentStatus.RELEASED;
        releasedAt = now;
        addDomainEvent(new PaymentReleased(getId(), engagementId, clientId, studentId, studentPayout().amount(), now));
        return true;
    }

    public void freeze() {
        if (status == PaymentStatus.HELD) {
            status = PaymentStatus.FROZEN;
        }
    }

    public void cancel() {
        if (status == PaymentStatus.PENDING) {
            status = PaymentStatus.CANCELLED;
        }
    }

    public Money totalCharged() {
        return amount.add(serviceFee);
    }

    public Money studentPayout() {
        return amount;
    }

    public UUID getEngagementId() {
        return engagementId;
    }

    public UUID getClientId() {
        return clientId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public Money getAmount() {
        return amount;
    }

    public Money getServiceFee() {
        return serviceFee;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Instant getHeldAt() {
        return heldAt;
    }

    public Instant getReleasedAt() {
        return releasedAt;
    }
}
