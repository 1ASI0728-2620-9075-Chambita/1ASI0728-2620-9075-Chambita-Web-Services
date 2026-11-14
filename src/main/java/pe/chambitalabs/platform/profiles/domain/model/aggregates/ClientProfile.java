package pe.chambitalabs.platform.profiles.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import pe.chambitalabs.platform.profiles.domain.model.valueobjects.RatingSummary;
import pe.chambitalabs.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.util.UUID;

/** Public profile of a client (entrepreneur or MYPE). */
@Entity
@Table(schema = "profiles", name = "client_profiles")
public class ClientProfile extends AuditableAbstractAggregateRoot<ClientProfile> {

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Column(name = "business_name", length = 120)
    private String businessName;

    @Column(length = 80)
    private String district;

    @Embedded
    private RatingSummary rating = RatingSummary.empty();

    protected ClientProfile() {
    }

    public ClientProfile(UUID userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    public void update(String displayName, String businessName, String district) {
        this.displayName = displayName;
        this.businessName = businessName;
        this.district = district;
    }

    public void updateRating(RatingSummary rating) {
        this.rating = rating;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBusinessName() {
        return businessName;
    }

    public String getDistrict() {
        return district;
    }

    public RatingSummary getRating() {
        return rating;
    }
}
