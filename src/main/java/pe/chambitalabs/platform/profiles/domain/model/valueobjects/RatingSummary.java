package pe.chambitalabs.platform.profiles.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

/** Local copy of the reputation, updated by RatingUpdated events. */
@Embeddable
public record RatingSummary(
        @Column(name = "rating_average", nullable = false, precision = 3, scale = 2) BigDecimal average,
        @Column(name = "rating_count", nullable = false) int count) {

    public static RatingSummary empty() {
        return new RatingSummary(BigDecimal.ZERO, 0);
    }

    public boolean isNew() {
        return count == 0;
    }
}
