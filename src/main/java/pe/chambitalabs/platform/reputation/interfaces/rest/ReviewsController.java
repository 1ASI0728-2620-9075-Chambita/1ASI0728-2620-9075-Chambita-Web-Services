package pe.chambitalabs.platform.reputation.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pe.chambitalabs.platform.reputation.application.internal.commandservices.ReviewCommandService;
import pe.chambitalabs.platform.reputation.domain.model.aggregates.Review;
import pe.chambitalabs.platform.reputation.domain.model.commands.ReviewCommands.SubmitReviewCommand;
import pe.chambitalabs.platform.reputation.infrastructure.persistence.jpa.repositories.ReputationSummaryRepository;
import pe.chambitalabs.platform.reputation.infrastructure.persistence.jpa.repositories.ReviewRepository;
import pe.chambitalabs.platform.reputation.interfaces.rest.resources.CreateReviewResource;
import pe.chambitalabs.platform.reputation.interfaces.rest.resources.ReputationResource;
import pe.chambitalabs.platform.reputation.interfaces.rest.resources.ReviewResource;
import pe.chambitalabs.platform.shared.infrastructure.security.AuthenticatedUser;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Reviews between clients and students, and accumulated reputation (TS09, US31, US32). */
@RestController
@Tag(name = "Reviews", description = "Mutual reviews and reputation")
public class ReviewsController {

    private final ReviewCommandService commandService;
    private final ReviewRepository reviewRepository;
    private final ReputationSummaryRepository summaryRepository;

    public ReviewsController(ReviewCommandService commandService, ReviewRepository reviewRepository,
                             ReputationSummaryRepository summaryRepository) {
        this.commandService = commandService;
        this.reviewRepository = reviewRepository;
        this.summaryRepository = summaryRepository;
    }

    @PostMapping("/api/v1/engagements/{engagementId}/reviews")
    @Operation(summary = "Rates the other party of a completed engagement with a score from 1 to 5")
    public ResponseEntity<ReviewResource> create(@PathVariable UUID engagementId,
                                                 @Valid @RequestBody CreateReviewResource resource,
                                                 @AuthenticationPrincipal AuthenticatedUser principal) {
        var review = commandService.handle(new SubmitReviewCommand(engagementId, principal.id(), resource.score(),
                resource.comment()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResource(review));
    }

    @GetMapping("/api/v1/users/{userId}/reviews")
    @Operation(summary = "Lists the reviews received by a user")
    public List<ReviewResource> byUser(@PathVariable UUID userId) {
        return reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(userId).stream()
                .map(ReviewsController::toResource).toList();
    }

    @GetMapping("/api/v1/users/{userId}/reputation")
    @Operation(summary = "Returns the average score and the number of reviews of a user")
    public ReputationResource reputation(@PathVariable UUID userId) {
        return summaryRepository.findByUserId(userId)
                .map(summary -> new ReputationResource(userId, summary.getAverageScore(), summary.getReviewCount()))
                .orElse(new ReputationResource(userId, BigDecimal.ZERO, 0));
    }

    private static ReviewResource toResource(Review review) {
        return new ReviewResource(review.getId(), review.getEngagementId(), review.getReviewerId(),
                review.getRevieweeId(), review.getReviewerRole().name(), review.getScore().value(),
                review.getComment(), review.getCreatedAt());
    }
}
