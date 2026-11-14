package pe.chambitalabs.platform.profiles.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record StudentProfileResource(UUID id, UUID userId, String fullName, String university, String career,
                                     Integer cycle, String bio, List<String> skills, Set<String> serviceCategories,
                                     BigDecimal ratingAverage, int ratingCount, boolean newInPlatform,
                                     int certificateCount, boolean complete, List<PortfolioItemResource> portfolio) {
}
