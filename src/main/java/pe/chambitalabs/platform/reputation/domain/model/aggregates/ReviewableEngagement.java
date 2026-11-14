package pe.chambitalabs.platform.reputation.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import pe.chambitalabs.platform.reputation.domain.model.events.ClientReviewed;
import pe.chambitalabs.platform.reputation.domain.model.events.StudentReviewed;
import pe.chambitalabs.platform.reputation.domain.model.valueobjects.ReviewerRole;
import pe.chambitalabs.platform.reputation.domain.model.valueobjects.Score;
import pe.chambitalabs.platform.shared.domain.exceptions.ConflictException;
import pe.chambitalabs.platform.shared.domain.exceptions.ForbiddenException;
import pe.chambitalabs.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.time.Instant;
import java.util.UUID;

/** Completed engagement that each party can rate once (US31, US32). */
@Entity
@Table(schema = "reputation", name = "reviewable_engagements")
public class ReviewableEngagement extends AuditableAbstractAggregateRoot<ReviewableEngagement> {

    @Column(name = "engagement_id", nullable = false, unique = true)
    private UUID engagementId;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "project_title", nullable = false, length = 120)
    private String projectTitle;

    @Column(nullable = false, length = 30)
    private String category;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @Column(name = "client_reviewed", nullable = false)
    private boolean clientReviewed;

    @Column(name = "student_reviewed", nullable = false)
    private boolean studentReviewed;

    protected ReviewableEngagement() {
    }

    public ReviewableEngagement(UUID engagementId, UUID clientId, UUID studentId, String projectTitle,
                                String category, Instant completedAt) {
        this.engagementId = engagementId;
        this.clientId = clientId;
        this.studentId = studentId;
        this.projectTitle = projectTitle;
        this.category = category;
        this.completedAt = completedAt;
    }

    /** Registers the review of one party and publishes the corresponding event. */
    public Review review(UUID reviewerId, Score score, String comment, Instant now) {
        if (clientId.equals(reviewerId)) {
            if (clientReviewed) {
                throw new ConflictException("reputation.already-reviewed");
            }
            clientReviewed = true;
            addDomainEvent(new StudentReviewed(engagementId, studentId, clientId, score.value(), projectTitle,
                    category, now));
            return new Review(engagementId, clientId, studentId, ReviewerRole.CLIENT, score, comment);
        }
        if (studentId.equals(reviewerId)) {
            if (studentReviewed) {
                throw new ConflictException("reputation.already-reviewed");
            }
            studentReviewed = true;
            addDomainEvent(new ClientReviewed(engagementId, clientId, studentId, score.value(), now));
            return new Review(engagementId, studentId, clientId, ReviewerRole.STUDENT, score, comment);
        }
        throw new ForbiddenException("reputation.not-participant");
    }

    public boolean isParticipant(UUID userId) {
        return clientId.equals(userId) || studentId.equals(userId);
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

    public boolean isClientReviewed() {
        return clientReviewed;
    }

    public boolean isStudentReviewed() {
        return studentReviewed;
    }
}
