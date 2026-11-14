package pe.chambitalabs.platform.profiles.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.profiles.domain.model.aggregates.ClientProfile;
import pe.chambitalabs.platform.profiles.domain.model.aggregates.StudentProfile;
import pe.chambitalabs.platform.profiles.domain.model.queries.ProfileQueries.GetClientProfileByUserIdQuery;
import pe.chambitalabs.platform.profiles.domain.model.queries.ProfileQueries.GetStudentProfileByUserIdQuery;
import pe.chambitalabs.platform.profiles.domain.model.queries.ProfileQueries.GetStudentProfilesByUserIdsQuery;
import pe.chambitalabs.platform.profiles.domain.services.ProfileQueryService;
import pe.chambitalabs.platform.profiles.infrastructure.persistence.jpa.repositories.ClientProfileRepository;
import pe.chambitalabs.platform.profiles.infrastructure.persistence.jpa.repositories.StudentProfileRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProfileQueryServiceImpl implements ProfileQueryService {

    private final StudentProfileRepository studentProfileRepository;
    private final ClientProfileRepository clientProfileRepository;

    public ProfileQueryServiceImpl(StudentProfileRepository studentProfileRepository,
                                   ClientProfileRepository clientProfileRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.clientProfileRepository = clientProfileRepository;
    }

    @Override
    public Optional<StudentProfile> handle(GetStudentProfileByUserIdQuery query) {
        return studentProfileRepository.findByUserId(query.userId());
    }

    @Override
    public List<StudentProfile> handle(GetStudentProfilesByUserIdsQuery query) {
        return query.userIds().isEmpty() ? List.of() : studentProfileRepository.findByUserIdIn(query.userIds());
    }

    @Override
    public Optional<ClientProfile> handle(GetClientProfileByUserIdQuery query) {
        return clientProfileRepository.findByUserId(query.userId());
    }
}
