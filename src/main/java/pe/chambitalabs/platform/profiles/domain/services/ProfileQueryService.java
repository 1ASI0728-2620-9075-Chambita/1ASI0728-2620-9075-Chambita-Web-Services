package pe.chambitalabs.platform.profiles.domain.services;

import pe.chambitalabs.platform.profiles.domain.model.aggregates.ClientProfile;
import pe.chambitalabs.platform.profiles.domain.model.aggregates.StudentProfile;
import pe.chambitalabs.platform.profiles.domain.model.queries.ProfileQueries.GetClientProfileByUserIdQuery;
import pe.chambitalabs.platform.profiles.domain.model.queries.ProfileQueries.GetStudentProfileByUserIdQuery;
import pe.chambitalabs.platform.profiles.domain.model.queries.ProfileQueries.GetStudentProfilesByUserIdsQuery;

import java.util.List;
import java.util.Optional;

public interface ProfileQueryService {

    Optional<StudentProfile> handle(GetStudentProfileByUserIdQuery query);

    List<StudentProfile> handle(GetStudentProfilesByUserIdsQuery query);

    Optional<ClientProfile> handle(GetClientProfileByUserIdQuery query);
}
