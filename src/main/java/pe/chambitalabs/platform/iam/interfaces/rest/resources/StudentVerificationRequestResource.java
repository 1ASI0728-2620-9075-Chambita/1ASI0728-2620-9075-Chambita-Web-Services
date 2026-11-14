package pe.chambitalabs.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record StudentVerificationRequestResource(@NotBlank @Email String email) {
}
