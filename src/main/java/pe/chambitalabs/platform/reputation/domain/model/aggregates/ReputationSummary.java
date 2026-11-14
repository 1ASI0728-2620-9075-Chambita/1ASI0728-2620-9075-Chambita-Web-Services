package pe.chambitalabs.platform.reputation.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import pe.chambitalabs.platform.reputation.domain.model.events.RatingUpdated;
import pe.chambitalabs.platform.reputation.domain.model.valueobjects.Score;
import pe.chambitalabs.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

/** Accumulated reputation of a user (student or client). */
@Entity
@Table(schema = "reputation", name = "reputation_summaries")
public class ReputationSummary extends AuditableAbstractAggregateRoot<ReputationSummary> {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "average_score", nullable = false, precision = 3, scale = 2)
    private BigDecimal averageScore = BigDecimal.ZERO;

    @Column(name = "review_count", nullable = false)
    private int reviewCount;

    protected ReputationSummary() {
    }

    public ReputationSummary(UUID userId) {
        this.userId = userId;
    }

    public void addScore(Score score) {
        var total = averageScore.multiply(BigDecimal.valueOf(reviewCount)).add(BigDecimal.valueOf(score.value()));
        reviewCount++;
        averageScore = total.divide(BigDecimal.valueOf(reviewCount), 2, RoundingMode.HALF_UP);
        addDomainEvent(new RatingUpdated(userId, averageScore, reviewCount, Instant.now()));
    }

    public UUID getUserId() {
        return userId;
    }

    public BigDecimal getAverageScore() {
        return averageScore;
    }

    public int getReviewCount() {
        return reviewCount;
    }
}
