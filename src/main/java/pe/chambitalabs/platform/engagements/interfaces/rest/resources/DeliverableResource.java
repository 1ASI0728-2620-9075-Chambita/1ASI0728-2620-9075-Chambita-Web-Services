package pe.chambitalabs.platform.engagements.interfaces.rest.resources;

import java.time.Instant;
import java.util.UUID;

public record DeliverableResource(UUID id, String fileUrl, String linkUrl, String note, String status,
                                  Instant submittedAt, Instant reviewedAt) {
}
