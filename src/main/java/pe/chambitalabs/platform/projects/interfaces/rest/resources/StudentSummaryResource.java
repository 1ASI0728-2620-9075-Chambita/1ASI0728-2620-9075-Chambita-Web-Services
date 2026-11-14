package pe.chambitalabs.platform.projects.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.UUID;

public record StudentSummaryResource(UUID id, String fullName, String career, Integer cycle, BigDecimal ratingAverage,
                                     int ratingCount, boolean newInPlatform, int certificateCount) {
}
