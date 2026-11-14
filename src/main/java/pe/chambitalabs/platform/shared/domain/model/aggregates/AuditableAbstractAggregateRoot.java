package pe.chambitalabs.platform.shared.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class of every aggregate: UUID identity assigned by the domain, audit dates and
 * domain events that Spring Data publishes when the aggregate is saved.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableAbstractAggregateRoot<T extends AbstractAggregateRoot<T>>
        extends AbstractAggregateRoot<T> implements Persistable<UUID> {

    @Id
    private UUID id = UUID.randomUUID();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    protected void addDomainEvent(Object event) {
        registerEvent(event);
    }

    /** Pending domain events, exposed for unit tests of the aggregates. */
    public java.util.Collection<Object> domainEventsForTest() {
        return domainEvents();
    }
}
