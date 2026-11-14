package pe.chambitalabs.platform.iam.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.iam.application.internal.outboundservices.HashingService;
import pe.chambitalabs.platform.iam.application.internal.outboundservices.MailService;
import pe.chambitalabs.platform.iam.domain.model.aggregates.StudentVerification;
import pe.chambitalabs.platform.iam.domain.model.aggregates.User;
import pe.chambitalabs.platform.iam.domain.model.commands.ConfirmStudentVerificationCommand;
import pe.chambitalabs.platform.iam.domain.model.commands.RequestStudentVerificationCommand;
import pe.chambitalabs.platform.iam.domain.model.valueobjects.EmailAddress;
import pe.chambitalabs.platform.iam.domain.model.valueobjects.Role;
import pe.chambitalabs.platform.iam.domain.model.valueobjects.VerificationStatus;
import pe.chambitalabs.platform.iam.domain.services.StudentVerificationCommandService;
import pe.chambitalabs.platform.iam.infrastructure.persistence.jpa.repositories.StudentVerificationRepository;
import pe.chambitalabs.platform.iam.infrastructure.persistence.jpa.repositories.UniversityDomainRepository;
import pe.chambitalabs.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import pe.chambitalabs.platform.shared.domain.exceptions.BadRequestException;
import pe.chambitalabs.platform.shared.domain.exceptions.BusinessRuleException;
import pe.chambitalabs.platform.shared.domain.exceptions.ConflictException;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;

import java.security.SecureRandom;
import java.time.Instant;

/** Generates, sends and confirms the institutional email verification code (TS02, US09). */
@Service
public class StudentVerificationCommandServiceImpl implements StudentVerificationCommandService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final StudentVerificationRepository verificationRepository;
    private final UniversityDomainRepository universityDomainRepository;
    private final HashingService hashingService;
    private final MailService mailService;

    public StudentVerificationCommandServiceImpl(UserRepository userRepository,
                                                 StudentVerificationRepository verificationRepository,
                                                 UniversityDomainRepository universityDomainRepository,
                                                 HashingService hashingService, MailService mailService) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.universityDomainRepository = universityDomainRepository;
        this.hashingService = hashingService;
        this.mailService = mailService;
    }

    @Override
    @Transactional
    public void handle(RequestStudentVerificationCommand command) {
        var email = new EmailAddress(command.email());
        if (universityDomainRepository.findMatching(email.domain()).isEmpty()) {
            throw new BusinessRuleException("iam.verification.not-institutional");
        }
        var user = userRepository.findByEmail(email)
                .filter(found -> found.getRole() == Role.STUDENT)
                .orElseThrow(() -> new ResourceNotFoundException("iam.user.not-found"));
        if (user.isStudentVerified()) {
            throw new ConflictException("iam.verification.already-verified");
        }
        sendNewCode(user);
    }

    /** Invalidates pending codes, stores a new hashed code and emails it. */
    @Transactional
    public void sendNewCode(User user) {
        verificationRepository.findByUserIdAndStatus(user.getId(), VerificationStatus.PENDING)
                .forEach(StudentVerification::expire);
        var code = "%06d".formatted(RANDOM.nextInt(1_000_000));
        verificationRepository.save(new StudentVerification(user.getId(), user.getEmail().value(),
                hashingService.encode(code), Instant.now()));
        mailService.sendVerificationCode(user.getEmail().value(), user.getFullName(), code);
    }

    @Override
    @Transactional(noRollbackFor = BadRequestException.class)
    public void handle(ConfirmStudentVerificationCommand command) {
        var email = new EmailAddress(command.email());
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("iam.user.not-found"));
        if (user.isStudentVerified()) {
            throw new ConflictException("iam.verification.already-verified");
        }
        var verification = verificationRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), VerificationStatus.PENDING)
                .orElseThrow(() -> new BadRequestException("iam.verification.code-expired"));
        var now = Instant.now();
        if (verification.isExpired(now)) {
            verification.expire();
            throw new BadRequestException("iam.verification.code-expired");
        }
        if (command.code() == null || !hashingService.matches(command.code(), verification.getCodeHash())) {
            verification.registerFailedAttempt();
            throw new BadRequestException("iam.verification.code-invalid");
        }
        verification.confirm(now);
        var university = universityDomainRepository.findMatching(email.domain())
                .map(domain -> domain.getUniversityName()).orElse("");
        user.markStudentVerified(university);
        userRepository.save(user);
    }
}
