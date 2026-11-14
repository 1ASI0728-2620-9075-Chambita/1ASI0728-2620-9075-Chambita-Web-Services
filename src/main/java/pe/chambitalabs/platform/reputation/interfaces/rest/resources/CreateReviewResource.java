package pe.chambitalabs.platform.reputation.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReviewResource(@NotNull Integer score, @Size(max = 600) String comment) {
}
