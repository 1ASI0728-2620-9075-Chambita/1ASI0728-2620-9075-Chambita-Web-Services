package pe.chambitalabs.platform.reputation.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import pe.chambitalabs.platform.shared.domain.exceptions.BadRequestException;

/** Score from 1 to 5. */
@Embeddable
public record Score(@Column(name = "score", nullable = false) int value) {

    public Score {
        if (value < 1 || value > 5) {
            throw new BadRequestException("reputation.score-out-of-range");
        }
    }
}
