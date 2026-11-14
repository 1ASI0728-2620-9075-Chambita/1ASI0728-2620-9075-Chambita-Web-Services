package pe.chambitalabs.platform.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record StudentVerificationConfirmationResource(@NotBlank @Email String email,
                                                      @NotBlank @Pattern(regexp = "\\d{6}") String code) {
}
