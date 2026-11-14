package pe.chambitalabs.platform.iam.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.iam.application.internal.outboundservices.HashingService;
import pe.chambitalabs.platform.iam.application.internal.outboundservices.TokenService;
import pe.chambitalabs.platform.iam.domain.model.aggregates.User;
import pe.chambitalabs.platform.iam.domain.model.commands.SignInCommand;
import pe.chambitalabs.platform.iam.domain.model.commands.SignUpCommand;
import pe.chambitalabs.platform.iam.domain.model.valueobjects.EmailAddress;
import pe.chambitalabs.platform.iam.domain.model.valueobjects.Role;
import pe.chambitalabs.platform.iam.domain.services.UserCommandService;
import pe.chambitalabs.platform.iam.infrastructure.persistence.jpa.repositories.UniversityDomainRepository;
import pe.chambitalabs.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import pe.chambitalabs.platform.shared.domain.exceptions.BusinessRuleException;
import pe.chambitalabs.platform.shared.domain.exceptions.ConflictException;
import pe.chambitalabs.platform.shared.domain.exceptions.UnauthorizedException;

/** Registers users and signs them in (TS01, US09, US10, US11). */
@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final UniversityDomainRepository universityDomainRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;

    public UserCommandServiceImpl(UserRepository userRepository, UniversityDomainRepository universityDomainRepository,
                                  HashingService hashingService, TokenService tokenService) {
        this.userRepository = userRepository;
        this.universityDomainRepository = universityDomainRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional
    public User handle(SignUpCommand command) {
        if (command.role() == Role.ADMIN) {
            throw new BusinessRuleException("iam.sign-up.role-not-allowed");
        }
        var email = new EmailAddress(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("iam.sign-up.email-in-use");
        }
        if (command.role() == Role.STUDENT && universityDomainRepository.findMatching(email.domain()).isEmpty()) {
            throw new BusinessRuleException("iam.sign-up.institutional-email-required");
        }
        var user = User.register(email, hashingService.encode(command.password()), command.fullName(), command.role());
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedSession handle(SignInCommand command) {
        EmailAddress email;
        try {
            email = new EmailAddress(command.email());
        } catch (IllegalArgumentException exception) {
            throw new UnauthorizedException("iam.sign-in.invalid-credentials");
        }
        var user = userRepository.findByEmail(email)
                .filter(found -> hashingService.matches(command.password(), found.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException("iam.sign-in.invalid-credentials"));
        return new AuthenticatedSession(user, tokenService.generateToken(user));
    }
}
