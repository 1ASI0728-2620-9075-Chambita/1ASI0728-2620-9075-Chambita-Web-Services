package pe.chambitalabs.platform.reputation.domain.model.commands;

import java.time.Instant;
import java.util.UUID;

/** Commands of the Reputation context. */
public final class ReviewCommands {

    private ReviewCommands() {
    }

    public record RegisterReviewableEngagementCommand(UUID engagementId, UUID clientId, UUID studentId,
                                                      String projectTitle, String category, Instant completedAt) {
    }

    public record SubmitReviewCommand(UUID engagementId, UUID reviewerId, int score, String comment) {
    }
}
