package pe.chambitalabs.platform.iam.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** Recognized university email domain (seeded by the Flyway migration). */
@Entity
@Table(schema = "iam", name = "university_domains")
public class UniversityDomain {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String domain;

    @Column(name = "university_name", nullable = false, length = 160)
    private String universityName;

    protected UniversityDomain() {
    }

    public boolean matches(String emailDomain) {
        return emailDomain.equals(domain) || emailDomain.endsWith("." + domain);
    }

    public String getDomain() {
        return domain;
    }

    public String getUniversityName() {
        return universityName;
    }
}
