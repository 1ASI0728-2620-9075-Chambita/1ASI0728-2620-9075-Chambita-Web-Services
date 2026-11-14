package pe.chambitalabs.platform.profiles.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CreatePortfolioItemResource(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 600) String description,
        @URL @Size(max = 500) String imageUrl,
        @URL @Size(max = 500) String externalUrl) {
}
