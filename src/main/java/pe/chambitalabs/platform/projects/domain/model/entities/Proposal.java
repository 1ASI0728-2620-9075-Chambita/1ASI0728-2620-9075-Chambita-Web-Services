package pe.chambitalabs.platform.projects.domain.model.entities;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pe.chambitalabs.platform.projects.domain.model.valueobjects.ProposalStatus;
import pe.chambitalabs.platform.shared.domain.model.valueobjects.Money;

import java.time.Instant;
import java.util.UUID;

/** Offer of a student to do a project. */
@Entity
@Table(schema = "projects", name = "proposals")
public class Proposal {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false))})
    private Money price;

    @Column(name = "delivery_days", nullable = false)
    private int deliveryDays;

    @Column(nullable = false, length = 600)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProposalStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Proposal() {
    }

    public Proposal(UUID studentId, Money price, int deliveryDays, String message) {
        this.studentId = studentId;
        this.price = price;
        this.deliveryDays = deliveryDays;
        this.message = message;
        this.status = ProposalStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public boolean isPending() {
        return status == ProposalStatus.PENDING;
    }

    public void accept() {
        status = ProposalStatus.ACCEPTED;
    }

    public void decline() {
        status = ProposalStatus.DECLINED;
    }

    public void close() {
        status = ProposalStatus.CLOSED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public Money getPrice() {
        return price;
    }

    public int getDeliveryDays() {
        return deliveryDays;
    }

    public String getMessage() {
        return message;
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
