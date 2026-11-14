package pe.chambitalabs.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.chambitalabs.platform.iam.domain.model.valueobjects.Role;

public record SignUpResource(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotBlank @Size(max = 120) String fullName,
        @NotNull Role role) {
}
