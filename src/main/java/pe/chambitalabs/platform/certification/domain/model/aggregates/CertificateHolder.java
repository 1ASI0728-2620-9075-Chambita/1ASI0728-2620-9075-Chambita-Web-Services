package pe.chambitalabs.platform.certification.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import pe.chambitalabs.platform.certification.domain.model.valueobjects.HolderId;
import pe.chambitalabs.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.util.UUID;

/** Pseudonymous holder of the certificates of a student. */
@Entity
@Table(schema = "certification", name = "certificate_holders")
public class CertificateHolder extends AuditableAbstractAggregateRoot<CertificateHolder> {

    @Column(name = "student_id", nullable = false, unique = true)
    private UUID studentId;

    @Column(name = "holder_id", nullable = false, unique = true)
    private UUID holderId;

    @Column(name = "holder_address", nullable = false, length = 42)
    private String holderAddress;

    protected CertificateHolder() {
    }

    public static CertificateHolder forStudent(UUID studentId) {
        var holder = new CertificateHolder();
        holder.studentId = studentId;
        holder.holderId = UUID.randomUUID();
        holder.holderAddress = new HolderId(holder.holderId).toEthereumAddress();
        return holder;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getHolderId() {
        return holderId;
    }

    public String getHolderAddress() {
        return holderAddress;
    }
}
