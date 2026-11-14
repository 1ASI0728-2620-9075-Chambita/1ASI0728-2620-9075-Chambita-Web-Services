package pe.chambitalabs.platform.projects.interfaces.rest.resources;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProposalResource(
        @NotNull @DecimalMin("1.00") @DecimalMax("99999.99") BigDecimal price,
        @NotNull @Min(1) @Max(180) Integer deliveryDays,
        @NotBlank @Size(max = 600) String message) {
}
