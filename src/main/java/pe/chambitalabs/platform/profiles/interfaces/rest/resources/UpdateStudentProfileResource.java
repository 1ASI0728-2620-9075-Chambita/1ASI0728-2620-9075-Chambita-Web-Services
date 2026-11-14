package pe.chambitalabs.platform.profiles.interfaces.rest.resources;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.chambitalabs.platform.profiles.domain.model.valueobjects.ServiceCategory;

import java.util.List;

public record UpdateStudentProfileResource(
        @NotBlank @Size(max = 120) String career,
        @NotNull @Min(1) @Max(14) Integer cycle,
        @Size(max = 600) String bio,
        @NotNull @Size(max = 20) List<@NotBlank @Size(max = 40) String> skills,
        @NotNull List<@NotNull ServiceCategory> serviceCategories) {
}
