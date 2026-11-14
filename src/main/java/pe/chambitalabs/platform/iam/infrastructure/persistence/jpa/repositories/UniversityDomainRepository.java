package pe.chambitalabs.platform.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.chambitalabs.platform.iam.domain.model.aggregates.UniversityDomain;

import java.util.Optional;
import java.util.UUID;

public interface UniversityDomainRepository extends JpaRepository<UniversityDomain, UUID> {

    /** Finds the university of an email domain, accepting subdomains (e.g. alumnos.upc.edu.pe). */
    default Optional<UniversityDomain> findMatching(String emailDomain) {
        return findAll().stream().filter(domain -> domain.matches(emailDomain)).findFirst();
    }
}
