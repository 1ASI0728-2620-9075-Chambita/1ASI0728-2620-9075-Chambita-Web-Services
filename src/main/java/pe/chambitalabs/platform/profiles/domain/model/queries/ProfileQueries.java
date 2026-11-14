package pe.chambitalabs.platform.profiles.domain.model.queries;

import pe.chambitalabs.platform.profiles.domain.model.valueobjects.ServiceCategory;

import java.util.Collection;
import java.util.UUID;

/** Queries of the Profiles context. */
public final class ProfileQueries {

    private ProfileQueries() {
    }

    public record GetStudentProfileByUserIdQuery(UUID userId) {
    }

    public record GetStudentProfilesByUserIdsQuery(Collection<UUID> userIds) {
    }

    public record GetStudentIdsByCategoryQuery(ServiceCategory category) {
    }

    public record GetClientProfileByUserIdQuery(UUID userId) {
    }
}
