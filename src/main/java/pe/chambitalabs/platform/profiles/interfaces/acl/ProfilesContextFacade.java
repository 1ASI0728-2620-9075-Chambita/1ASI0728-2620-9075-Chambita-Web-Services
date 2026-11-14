package pe.chambitalabs.platform.profiles.interfaces.acl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.profiles.domain.model.aggregates.StudentProfile;
import pe.chambitalabs.platform.profiles.domain.model.valueobjects.ServiceCategory;
import pe.chambitalabs.platform.profiles.infrastructure.persistence.jpa.repositories.ClientProfileRepository;
import pe.chambitalabs.platform.profiles.infrastructure.persistence.jpa.repositories.StudentProfileRepository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Exposes to Projects, Notifications and Assistance the eligibility, categories, skills,
 * ratings and certificates of students.
 */
@Service
@Transactional(readOnly = true)
public class ProfilesContextFacade {

    private final StudentProfileRepository studentProfileRepository;
    private final ClientProfileRepository clientProfileRepository;

    public ProfilesContextFacade(StudentProfileRepository studentProfileRepository,
                                 ClientProfileRepository clientProfileRepository) {
        this.studentProfileRepository = studentProfileRepository;
        this.clientProfileRepository = clientProfileRepository;
    }

    /** A student can send proposals when verified (has a profile) and the profile has a category. */
    public boolean isStudentEligible(UUID userId) {
        return studentProfileRepository.findByUserId(userId).map(StudentProfile::isComplete).orElse(false);
    }

    public Set<String> fetchServiceCategories(UUID userId) {
        return studentProfileRepository.findByUserId(userId)
                .map(profile -> profile.getServiceCategories().stream().map(Enum::name)
                        .collect(Collectors.toCollection(java.util.LinkedHashSet::new)))
                .orElse(new java.util.LinkedHashSet<>());
    }

    public List<UUID> fetchStudentIdsByCategory(String category) {
        var serviceCategory = ServiceCategory.valueOf(category);
        return studentProfileRepository.findAll().stream()
                .filter(profile -> profile.getServiceCategories().contains(serviceCategory))
                .map(StudentProfile::getUserId).toList();
    }

    public Map<UUID, StudentSnapshot> fetchStudentSnapshots(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return studentProfileRepository.findByUserIdIn(userIds).stream()
                .map(ProfilesContextFacade::toSnapshot)
                .collect(Collectors.toMap(StudentSnapshot::userId, Function.identity()));
    }

    public Optional<StudentSnapshot> fetchStudentSnapshot(UUID userId) {
        return studentProfileRepository.findByUserId(userId).map(ProfilesContextFacade::toSnapshot);
    }

    public Optional<String> fetchClientDisplayName(UUID userId) {
        return clientProfileRepository.findByUserId(userId).map(profile ->
                profile.getBusinessName() != null && !profile.getBusinessName().isBlank()
                        ? profile.getBusinessName() : profile.getDisplayName());
    }

    private static StudentSnapshot toSnapshot(StudentProfile profile) {
        return new StudentSnapshot(profile.getUserId(), profile.getFullName(), profile.getCareer(), profile.getCycle(),
                profile.getSkills(), profile.getServiceCategories().stream().map(Enum::name).toList(),
                profile.getRating().average(), profile.getRating().count(), profile.getCertificateCount());
    }

    /** Read-only view of a student shared with other contexts. */
    public record StudentSnapshot(UUID userId, String fullName, String career, Integer cycle, List<String> skills,
                                  List<String> serviceCategories, BigDecimal ratingAverage, int ratingCount,
                                  int certificateCount) {
    }
}
