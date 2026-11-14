package pe.chambitalabs.platform.profiles.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateClientProfileResource(@NotBlank @Size(max = 120) String displayName,
                                          @Size(max = 120) String businessName,
                                          @Size(max = 80) String district) {
}
