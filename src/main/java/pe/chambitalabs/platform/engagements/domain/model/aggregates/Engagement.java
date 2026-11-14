package pe.chambitalabs.platform.engagements.domain.model.aggregates;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import pe.chambitalabs.platform.engagements.domain.model.entities.Deliverable;
import pe.chambitalabs.platform.engagements.domain.model.events.DeliverableApproved;
import pe.chambitalabs.platform.engagements.domain.model.events.DeliverableSubmitted;
import pe.chambitalabs.platform.engagements.domain.model.events.EngagementCancelled;
import pe.chambitalabs.platform.engagements.domain.model.events.EngagementCompleted;
import pe.chambitalabs.platform.engagements.domain.model.events.EngagementCreated;
import pe.chambitalabs.platform.engagements.domain.model.valueobjects.DeliverableStatus;
import pe.chambitalabs.platform.engagements.domain.model.valueobjects.EngagementStatus;
import pe.chambitalabs.platform.shared.domain.exceptions.BadRequestException;
import pe.chambitalabs.platform.shared.domain.exceptions.ConflictException;
import pe.chambitalabs.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import pe.chambitalabs.platform.shared.domain.model.valueobjects.Money;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Engagement between a client and a student, created when a proposal is accepted.
 * PENDING_PAYMENT → ACTIVE (payment held) → IN_REVIEW (work delivered) → COMPLETED (payment released).
 */
@Entity
@Table(schema = "engagements", name = "engagements")
public class Engagement extends AuditableAbstractAggregateRoot<Engagement> {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "proposal_id", nullable = false, unique = true)
    private UUID proposalId;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "project_title", nullable = false, length = 120)
    private String projectTitle;

    @Column(nullable = false, length = 30)
    private String category;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "agreed_price_amount", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "agreed_price_currency", nullable = false))})
    private Money agreedPrice;

    @Column(name = "delivery_days", nullable = false)
    private int deliveryDays;

    @Column(nullable = false)
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EngagementStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "engagement_id", nullable = false)
    @OrderBy("submittedAt ASC")
    private List<Deliverable> deliverables = new ArrayList<>();

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Version
    private long version;

    protected Engagement() {
    }

    public static Engagement fromAcceptedProposal(UUID projectId, UUID proposalId, UUID clientId, UUID studentId,
                                                  String projectTitle, String category, Money agreedPrice,
                                                  int deliveryDays, LocalDate deadline) {
        var engagement = new Engagement();
        engagement.projectId = projectId;
        engagement.proposalId = proposalId;
        engagement.clientId = clientId;
        engagement.studentId = studentId;
        engagement.projectTitle = projectTitle;
        engagement.category = category;
        engagement.agreedPrice = agreedPrice;
        engagement.deliveryDays = deliveryDays;
        engagement.deadline = deadline;
        engagement.status = EngagementStatus.PENDING_PAYMENT;
        engagement.addDomainEvent(new EngagementCreated(engagement.getId(), projectId, clientId, studentId,
                projectTitle, agreedPrice.amount(), agreedPrice.currency(), Instant.now()));
        return engagement;
    }

    /** The protected payment was held: the student can start working (idempotent). */
    public void activate(Instant now) {
        if (status != EngagementStatus.PENDING_PAYMENT) {
            return;
        }
        status = EngagementStatus.ACTIVE;
        activatedAt = now;
    }

    public Deliverable submitDeliverable(String fileUrl, String linkUrl, String note, Instant now) {
        requireStatus(EngagementStatus.ACTIVE);
        if (isBlank(fileUrl) && isBlank(linkUrl)) {
            throw new BadRequestException("engagements.deliverable.empty");
        }
        var deliverable = new Deliverable(blankToNull(fileUrl), blankToNull(linkUrl), blankToNull(note), now);
        deliverables.add(deliverable);
        status = EngagementStatus.IN_REVIEW;
        addDomainEvent(new DeliverableSubmitted(getId(), deliverable.getId(), clientId, studentId, now));
        return deliverable;
    }

    /** Approves the delivered work; a second approval is rejected (QA05). */
    public Deliverable approveDeliverable(Instant now, boolean automatic) {
        requireStatus(EngagementStatus.IN_REVIEW);
        var deliverable = latestDeliverable()
                .filter(latest -> latest.getStatus() == DeliverableStatus.SUBMITTED)
                .orElseThrow(() -> new ConflictException("engagements.invalid-status", status.name()));
        deliverable.approve(now);
        addDomainEvent(new DeliverableApproved(getId(), deliverable.getId(), clientId, studentId, automatic, now));
        return deliverable;
    }

    /** Approves automatically when the client did not answer within the review period (US28). */
    public boolean autoApproveIfOverdue(Instant now, Duration reviewPeriod) {
        if (status != EngagementStatus.IN_REVIEW) {
            return false;
        }
        var pending = latestDeliverable().filter(latest -> latest.getStatus() == DeliverableStatus.SUBMITTED);
        if (pending.isEmpty() || pending.get().getSubmittedAt().plus(reviewPeriod).isAfter(now)) {
            return false;
        }
        approveDeliverable(now, true);
        return true;
    }

    /** The payment was released: the engagement is completed (idempotent). */
    public void complete(Instant now) {
        if (status == EngagementStatus.COMPLETED) {
            return;
        }
        requireStatus(EngagementStatus.IN_REVIEW);
        status = EngagementStatus.COMPLETED;
        completedAt = now;
        addDomainEvent(new EngagementCompleted(getId(), clientId, studentId, projectTitle, category, now));
    }

    public void cancel(Instant now) {
        requireStatus(EngagementStatus.PENDING_PAYMENT);
        status = EngagementStatus.CANCELLED;
        addDomainEvent(new EngagementCancelled(getId(), clientId, studentId, now));
    }

    public boolean isParticipant(UUID userId) {
        return clientId.equals(userId) || studentId.equals(userId);
    }

    public Optional<Deliverable> latestDeliverable() {
        return deliverables.isEmpty() ? Optional.empty() : Optional.of(deliverables.getLast());
    }

    private void requireStatus(EngagementStatus expected) {
        if (status != expected) {
            throw new ConflictException("engagements.invalid-status", status.name());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    public UUID getProjectId() {
        return projectId;
    }

    public UUID getProposalId() {
        return proposalId;
    }

    public UUID getClientId() {
        return clientId;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public String getProjectTitle() {
        return projectTitle;
    }

    public String getCategory() {
        return category;
    }

    public Money getAgreedPrice() {
        return agreedPrice;
    }

    public int getDeliveryDays() {
        return deliveryDays;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public EngagementStatus getStatus() {
        return status;
    }

    public List<Deliverable> getDeliverables() {
        return List.copyOf(deliverables);
    }

    public Instant getActivatedAt() {
        return activatedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
