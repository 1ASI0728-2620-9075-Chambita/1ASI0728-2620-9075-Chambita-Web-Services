package pe.chambitalabs.platform.engagements.domain.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pe.chambitalabs.platform.engagements.domain.model.valueobjects.DeliverableStatus;

import java.time.Instant;
import java.util.UUID;

/** Work delivered by the student. */
@Entity
@Table(schema = "engagements", name = "deliverables")
public class Deliverable {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "link_url", length = 500)
    private String linkUrl;

    @Column(length = 600)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliverableStatus status;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    protected Deliverable() {
    }

    public Deliverable(String fileUrl, String linkUrl, String note, Instant submittedAt) {
        this.fileUrl = fileUrl;
        this.linkUrl = linkUrl;
        this.note = note;
        this.status = DeliverableStatus.SUBMITTED;
        this.submittedAt = submittedAt;
    }

    public void approve(Instant now) {
        status = DeliverableStatus.APPROVED;
        reviewedAt = now;
    }

    public void requestChanges(Instant now) {
        status = DeliverableStatus.CHANGES_REQUESTED;
        reviewedAt = now;
    }

    public UUID getId() {
        return id;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public String getNote() {
        return note;
    }

    public DeliverableStatus getStatus() {
        return status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}
