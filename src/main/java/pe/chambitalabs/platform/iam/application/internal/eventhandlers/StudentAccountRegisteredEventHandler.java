package pe.chambitalabs.platform.iam.application.internal.eventhandlers;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import pe.chambitalabs.platform.iam.application.internal.commandservices.StudentVerificationCommandServiceImpl;
import pe.chambitalabs.platform.iam.domain.model.events.StudentAccountRegistered;
import pe.chambitalabs.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;

/** When a student registers, sends the verification code to the institutional email. */
@Component
public class StudentAccountRegisteredEventHandler {

    private final UserRepository userRepository;
    private final StudentVerificationCommandServiceImpl verificationService;

    public StudentAccountRegisteredEventHandler(UserRepository userRepository,
                                                StudentVerificationCommandServiceImpl verificationService) {
        this.userRepository = userRepository;
        this.verificationService = verificationService;
    }

    @ApplicationModuleListener
    public void on(StudentAccountRegistered event) {
        userRepository.findById(event.userId())
                .filter(user -> !user.isStudentVerified())
                .ifPresent(verificationService::sendNewCode);
    }
}
