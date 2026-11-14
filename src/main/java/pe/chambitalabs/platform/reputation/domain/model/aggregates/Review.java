package pe.chambitalabs.platform.reputation.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import pe.chambitalabs.platform.reputation.domain.model.valueobjects.ReviewerRole;
import pe.chambitalabs.platform.reputation.domain.model.valueobjects.Score;
import pe.chambitalabs.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.util.UUID;

/** Rating that one party gives to the other when an engagement is completed. */
@Entity
@Table(schema = "reputation", name = "reviews")
public class Review extends AuditableAbstractAggregateRoot<Review> {

    @Column(name = "engagement_id", nullable = false)
    private UUID engagementId;

    @Column(name = "reviewer_id", nullable = false)
    private UUID reviewerId;

    @Column(name = "reviewee_id", nullable = false)
    private UUID revieweeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reviewer_role", nullable = false, length = 20)
    private ReviewerRole reviewerRole;

    @Embedded
    private Score score;

    @Column(length = 600)
    private String comment;

    protected Review() {
    }

    public Review(UUID engagementId, UUID reviewerId, UUID revieweeId, ReviewerRole reviewerRole, Score score,
                  String comment) {
        this.engagementId = engagementId;
        this.reviewerId = reviewerId;
        this.revieweeId = revieweeId;
        this.reviewerRole = reviewerRole;
        this.score = score;
        this.comment = comment;
    }

    public UUID getEngagementId() {
        return engagementId;
    }

    public UUID getReviewerId() {
        return reviewerId;
    }

    public UUID getRevieweeId() {
        return revieweeId;
    }

    public ReviewerRole getReviewerRole() {
        return reviewerRole;
    }

    public Score getScore() {
        return score;
    }

    public String getComment() {
        return comment;
    }
}
