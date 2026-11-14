package pe.chambitalabs.platform.reputation.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.reputation.domain.model.aggregates.ReputationSummary;
import pe.chambitalabs.platform.reputation.domain.model.aggregates.Review;
import pe.chambitalabs.platform.reputation.domain.model.aggregates.ReviewableEngagement;
import pe.chambitalabs.platform.reputation.domain.model.commands.ReviewCommands.RegisterReviewableEngagementCommand;
import pe.chambitalabs.platform.reputation.domain.model.commands.ReviewCommands.SubmitReviewCommand;
import pe.chambitalabs.platform.reputation.domain.model.valueobjects.Score;
import pe.chambitalabs.platform.reputation.infrastructure.persistence.jpa.repositories.ReputationSummaryRepository;
import pe.chambitalabs.platform.reputation.infrastructure.persistence.jpa.repositories.ReviewRepository;
import pe.chambitalabs.platform.reputation.infrastructure.persistence.jpa.repositories.ReviewableEngagementRepository;
import pe.chambitalabs.platform.shared.domain.exceptions.ConflictException;

import java.time.Clock;
import java.time.Instant;

/** Validates and registers reviews and updates the reputation (TS09, US31, US32). */
@Service
@Transactional
public class ReviewCommandService {

    private final ReviewableEngagementRepository reviewableRepository;
    private final ReviewRepository reviewRepository;
    private final ReputationSummaryRepository summaryRepository;
    private final Clock clock;

    public ReviewCommandService(ReviewableEngagementRepository reviewableRepository, ReviewRepository reviewRepository,
                                ReputationSummaryRepository summaryRepository, Clock clock) {
        this.reviewableRepository = reviewableRepository;
        this.reviewRepository = reviewRepository;
        this.summaryRepository = summaryRepository;
        this.clock = clock;
    }

    public void handle(RegisterReviewableEngagementCommand command) {
        if (reviewableRepository.findByEngagementId(command.engagementId()).isEmpty()) {
            reviewableRepository.save(new ReviewableEngagement(command.engagementId(), command.clientId(),
                    command.studentId(), command.projectTitle(), command.category(), command.completedAt()));
        }
    }

    public Review handle(SubmitReviewCommand command) {
        var score = new Score(command.score());
        var reviewable = reviewableRepository.findByEngagementId(command.engagementId())
                .orElseThrow(() -> new ConflictException("reputation.engagement-not-reviewable"));
        var review = reviewable.review(command.reviewerId(), score, command.comment(), Instant.now(clock));
        reviewableRepository.saveAndFlush(reviewable);
        var saved = reviewRepository.save(review);
        var summary = summaryRepository.findByUserId(review.getRevieweeId())
                .orElseGet(() -> new ReputationSummary(review.getRevieweeId()));
        summary.addScore(score);
        summaryRepository.save(summary);
        return saved;
    }
}
