package pe.chambitalabs.platform.profiles.domain.model.aggregates;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.chambitalabs.platform.profiles.domain.model.entities.PortfolioItem;
import pe.chambitalabs.platform.profiles.domain.model.events.StudentProfileCompleted;
import pe.chambitalabs.platform.profiles.domain.model.valueobjects.RatingSummary;
import pe.chambitalabs.platform.profiles.domain.model.valueobjects.ServiceCategory;
import pe.chambitalabs.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Professional profile of a verified student, with skills, service categories and portfolio. */
@Entity
@Table(schema = "profiles", name = "student_profiles")
public class StudentProfile extends AuditableAbstractAggregateRoot<StudentProfile> {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(length = 160)
    private String university;

    @Column(length = 120)
    private String career;

    private Integer cycle;

    @Column(length = 600)
    private String bio;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "skills", columnDefinition = "text[]", nullable = false)
    private List<String> skills = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "service_categories", columnDefinition = "text[]", nullable = false)
    private List<String> serviceCategories = new ArrayList<>();

    @Embedded
    private RatingSummary rating = RatingSummary.empty();

    @Column(name = "certificate_count", nullable = false)
    private int certificateCount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "student_profile_id", nullable = false)
    @OrderBy("createdAt DESC")
    private List<PortfolioItem> portfolioItems = new ArrayList<>();

    protected StudentProfile() {
    }

    public StudentProfile(UUID userId, String fullName, String university) {
        this.userId = userId;
        this.fullName = fullName;
        this.university = university;
    }

    public void update(String career, Integer cycle, String bio, Collection<String> skills,
                       Collection<ServiceCategory> categories) {
        var wasComplete = isComplete();
        this.career = career;
        this.cycle = cycle;
        this.bio = bio;
        this.skills = new ArrayList<>(new LinkedHashSet<>(skills.stream().map(String::trim)
                .filter(skill -> !skill.isEmpty()).toList()));
        this.serviceCategories = new ArrayList<>(categories.stream().map(Enum::name).distinct().toList());
        if (!wasComplete && isComplete()) {
            addDomainEvent(new StudentProfileCompleted(getId(), userId, Instant.now()));
        }
    }

    /** A profile is complete (visible and able to send proposals) with at least one service category. */
    public boolean isComplete() {
        return !serviceCategories.isEmpty();
    }

    public PortfolioItem addPortfolioItem(String title, String description, String imageUrl, String externalUrl) {
        var item = new PortfolioItem(title, description, imageUrl, externalUrl);
        portfolioItems.add(item);
        return item;
    }

    public boolean removePortfolioItem(UUID itemId) {
        return portfolioItems.removeIf(item -> item.getId().equals(itemId));
    }

    public Optional<PortfolioItem> findPortfolioItem(UUID itemId) {
        return portfolioItems.stream().filter(item -> item.getId().equals(itemId)).findFirst();
    }

    public void updateRating(RatingSummary rating) {
        this.rating = rating;
    }

    public void registerCertificate() {
        this.certificateCount++;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUniversity() {
        return university;
    }

    public String getCareer() {
        return career;
    }

    public Integer getCycle() {
        return cycle;
    }

    public String getBio() {
        return bio;
    }

    public List<String> getSkills() {
        return List.copyOf(skills);
    }

    public Set<ServiceCategory> getServiceCategories() {
        var categories = new LinkedHashSet<ServiceCategory>();
        serviceCategories.forEach(name -> categories.add(ServiceCategory.valueOf(name)));
        return categories;
    }

    public RatingSummary getRating() {
        return rating;
    }

    public int getCertificateCount() {
        return certificateCount;
    }

    public List<PortfolioItem> getPortfolioItems() {
        return List.copyOf(portfolioItems);
    }
}
