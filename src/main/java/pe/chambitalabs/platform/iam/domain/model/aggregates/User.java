package pe.chambitalabs.platform.iam.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import pe.chambitalabs.platform.iam.domain.model.events.ClientAccountRegistered;
import pe.chambitalabs.platform.iam.domain.model.events.StudentAccountRegistered;
import pe.chambitalabs.platform.iam.domain.model.events.StudentVerified;
import pe.chambitalabs.platform.iam.domain.model.valueobjects.AccountStatus;
import pe.chambitalabs.platform.iam.domain.model.valueobjects.EmailAddress;
import pe.chambitalabs.platform.iam.domain.model.valueobjects.Role;
import pe.chambitalabs.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.time.Instant;

/** User account with its role and status. */
@Entity
@Table(schema = "iam", name = "users")
public class User extends AuditableAbstractAggregateRoot<User> {

    @Embedded
    private EmailAddress email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AccountStatus status;

    @Column(name = "student_verified", nullable = false)
    private boolean studentVerified;

    protected User() {
    }

    private User(EmailAddress email, String passwordHash, String fullName, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName.trim();
        this.role = role;
        this.status = role == Role.STUDENT ? AccountStatus.PENDING_VERIFICATION : AccountStatus.ACTIVE;
    }

    public static User register(EmailAddress email, String passwordHash, String fullName, Role role) {
        var user = new User(email, passwordHash, fullName, role);
        var now = Instant.now();
        if (role == Role.STUDENT) {
            user.addDomainEvent(new StudentAccountRegistered(user.getId(), email.value(), user.fullName, now));
        } else if (role == Role.CLIENT) {
            user.addDomainEvent(new ClientAccountRegistered(user.getId(), email.value(), user.fullName, now));
        }
        return user;
    }

    public void markStudentVerified(String university) {
        if (studentVerified) {
            return;
        }
        this.studentVerified = true;
        this.status = AccountStatus.ACTIVE;
        addDomainEvent(new StudentVerified(getId(), email.value(), fullName, university, Instant.now()));
    }

    public void changePassword(String newHash) {
        this.passwordHash = newHash;
    }

    public EmailAddress getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public boolean isStudentVerified() {
        return studentVerified;
    }
}
