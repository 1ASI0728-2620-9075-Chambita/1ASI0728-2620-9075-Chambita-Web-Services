package pe.chambitalabs.platform.projects.interfaces.rest.resources;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.chambitalabs.platform.projects.domain.model.valueobjects.ServiceCategory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record CreateProjectResource(
        @NotBlank @Size(max = 120) String title,
        @NotBlank @Size(max = 2000) String description,
        @NotNull ServiceCategory category,
        @Size(max = 15) List<@NotBlank @Size(max = 40) String> requiredSkills,
        @Size(max = 15) List<@NotBlank @Size(max = 160) String> deliverables,
        @NotNull @DecimalMin("1.00") @DecimalMax("99999.99") BigDecimal budget,
        @NotNull LocalDate deadline,
        Boolean assisted) {
}
