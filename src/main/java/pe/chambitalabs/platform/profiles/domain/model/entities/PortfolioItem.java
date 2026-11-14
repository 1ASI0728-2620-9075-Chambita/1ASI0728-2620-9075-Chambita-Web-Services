package pe.chambitalabs.platform.profiles.domain.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Work shown in a student's portfolio. */
@Entity
@Table(schema = "profiles", name = "portfolio_items")
public class PortfolioItem {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, length = 120)
    private String title;

    @Column(length = 600)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "external_url", length = 500)
    private String externalUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected PortfolioItem() {
    }

    public PortfolioItem(String title, String description, String imageUrl, String externalUrl) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.externalUrl = externalUrl;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
