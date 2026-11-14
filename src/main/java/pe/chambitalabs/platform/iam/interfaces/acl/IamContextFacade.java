package pe.chambitalabs.platform.iam.interfaces.acl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.iam.domain.model.aggregates.User;
import pe.chambitalabs.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;

import java.util.Optional;
import java.util.UUID;

/** Exposes the role, verification and display name of a user to other contexts. */
@Service
@Transactional(readOnly = true)
public class IamContextFacade {

    private final UserRepository userRepository;

    public IamContextFacade(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<String> fetchUserRole(UUID userId) {
        return userRepository.findById(userId).map(user -> user.getRole().name());
    }

    public boolean isStudentVerified(UUID userId) {
        return userRepository.findById(userId).map(User::isStudentVerified).orElse(false);
    }

    public Optional<String> fetchDisplayName(UUID userId) {
        return userRepository.findById(userId).map(User::getFullName);
    }
}
