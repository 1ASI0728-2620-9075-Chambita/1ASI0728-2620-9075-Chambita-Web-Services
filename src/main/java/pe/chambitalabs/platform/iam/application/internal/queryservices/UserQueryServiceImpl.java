package pe.chambitalabs.platform.iam.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.iam.domain.model.aggregates.User;
import pe.chambitalabs.platform.iam.domain.model.queries.GetUserByIdQuery;
import pe.chambitalabs.platform.iam.domain.services.UserQueryService;
import pe.chambitalabs.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId());
    }
}
