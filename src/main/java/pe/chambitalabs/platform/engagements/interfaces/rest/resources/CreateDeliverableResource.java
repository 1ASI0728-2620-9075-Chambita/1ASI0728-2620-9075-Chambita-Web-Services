package pe.chambitalabs.platform.engagements.interfaces.rest.resources;

import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

public record CreateDeliverableResource(@URL @Size(max = 500) String fileUrl,
                                        @URL @Size(max = 500) String linkUrl,
                                        @Size(max = 600) String note) {
}
