package pe.chambitalabs.platform.reputation.domain;

import org.junit.jupiter.api.Test;
import pe.chambitalabs.platform.reputation.domain.model.aggregates.ReputationSummary;
import pe.chambitalabs.platform.reputation.domain.model.aggregates.ReviewableEngagement;
import pe.chambitalabs.platform.reputation.domain.model.events.StudentReviewed;
import pe.chambitalabs.platform.reputation.domain.model.valueobjects.Score;
import pe.chambitalabs.platform.shared.domain.exceptions.BadRequestException;
import pe.chambitalabs.platform.shared.domain.exceptions.ConflictException;
import pe.chambitalabs.platform.shared.domain.exceptions.ForbiddenException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReviewableEngagementTest {

    private final UUID clientId = UUID.randomUUID();
    private final UUID studentId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-11-14T15:00:00Z");

    private ReviewableEngagement reviewable() {
        return new ReviewableEngagement(UUID.randomUUID(), clientId, studentId, "Identidad visual", "GRAPHIC_DESIGN", now);
    }

    @Test
    void theClientReviewTriggersStudentReviewed() {
        var reviewable = reviewable();
        var review = reviewable.review(clientId, new Score(5), "Excelente", now);
        assertThat(review.getRevieweeId()).isEqualTo(studentId);
        assertThat(reviewable.domainEventsForTest()).hasAtLeastOneElementOfType(StudentReviewed.class);
    }

    @Test
    void eachPartyReviewsOnlyOnceAndStrangersCannotReview() {
        var reviewable = reviewable();
        reviewable.review(studentId, new Score(4), null, now);
        assertThatThrownBy(() -> reviewable.review(studentId, new Score(4), null, now))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> reviewable.review(UUID.randomUUID(), new Score(4), null, now))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void scoresMustBeBetweenOneAndFive() {
        assertThatThrownBy(() -> new Score(0)).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> new Score(6)).isInstanceOf(BadRequestException.class);
    }

    @Test
    void computesTheAverageScore() {
        var summary = new ReputationSummary(studentId);
        summary.addScore(new Score(5));
        summary.addScore(new Score(4));
        assertThat(summary.getAverageScore()).isEqualByComparingTo("4.50");
        assertThat(summary.getReviewCount()).isEqualTo(2);
    }
}
