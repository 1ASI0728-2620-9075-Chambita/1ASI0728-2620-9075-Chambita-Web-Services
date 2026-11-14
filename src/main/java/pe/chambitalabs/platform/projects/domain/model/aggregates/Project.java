package pe.chambitalabs.platform.projects.domain.model.aggregates;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pe.chambitalabs.platform.projects.domain.model.entities.Proposal;
import pe.chambitalabs.platform.projects.domain.model.events.ProjectPublished;
import pe.chambitalabs.platform.projects.domain.model.events.ProposalAccepted;
import pe.chambitalabs.platform.projects.domain.model.events.ProposalDeclined;
import pe.chambitalabs.platform.projects.domain.model.events.ProposalSubmitted;
import pe.chambitalabs.platform.projects.domain.model.valueobjects.ProjectStatus;
import pe.chambitalabs.platform.projects.domain.model.valueobjects.ServiceCategory;
import pe.chambitalabs.platform.shared.domain.exceptions.ConflictException;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;
import pe.chambitalabs.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import pe.chambitalabs.platform.shared.domain.model.valueobjects.Money;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Need published by a client, with the proposals of the students (US16, US19, US21). */
@Entity
@Table(schema = "projects", name = "projects")
public class Project extends AuditableAbstractAggregateRoot<Project> {

    public static final String REASON_DECLINED = "DECLINED";
    public static final String REASON_CLOSED = "CLOSED";

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ServiceCategory category;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "required_skills", columnDefinition = "text[]", nullable = false)
    private List<String> requiredSkills = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "deliverables", columnDefinition = "text[]", nullable = false)
    private List<String> deliverables = new ArrayList<>();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "budget_amount", nullable = false)),
            @AttributeOverride(name = "currency", column = @Column(name = "budget_currency", nullable = false))})
    private Money budget;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(nullable = false)
    private boolean assisted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @OrderBy("createdAt ASC")
    private List<Proposal> proposals = new ArrayList<>();

    @Version
    private long version;

    protected Project() {
    }

    private Project(UUID clientId, String title, String description, ServiceCategory category,
                    List<String> requiredSkills, List<String> deliverables, Money budget, LocalDate deadline,
                    boolean assisted) {
        this.clientId = clientId;
        this.title = title.trim();
        this.description = description.trim();
        this.category = category;
        this.requiredSkills = clean(requiredSkills);
        this.deliverables = clean(deliverables);
        this.budget = budget;
        this.deadline = deadline;
        this.assisted = assisted;
        this.status = ProjectStatus.OPEN;
    }

    public static Project publish(UUID clientId, String title, String description, ServiceCategory category,
                                  List<String> requiredSkills, List<String> deliverables, Money budget,
                                  LocalDate deadline, boolean assisted) {
        var project = new Project(clientId, title, description, category, requiredSkills, deliverables, budget,
                deadline, assisted);
        project.addDomainEvent(new ProjectPublished(project.getId(), clientId, project.title, category.name(),
                budget.amount(), Instant.now()));
        return project;
    }

    public Proposal submitProposal(UUID studentId, Money price, int deliveryDays, String message) {
        if (status != ProjectStatus.OPEN) {
            throw new ConflictException("projects.project-not-open");
        }
        if (proposals.stream().anyMatch(proposal -> proposal.getStudentId().equals(studentId))) {
            throw new ConflictException("projects.proposal-duplicated");
        }
        var proposal = new Proposal(studentId, price, deliveryDays, message.trim());
        proposals.add(proposal);
        addDomainEvent(new ProposalSubmitted(getId(), proposal.getId(), clientId, studentId, title, Instant.now()));
        return proposal;
    }

    /** Accepts one proposal; the others are closed and their students are notified (US21). */
    public Proposal acceptProposal(UUID proposalId, Clock clock) {
        if (status != ProjectStatus.OPEN) {
            throw new ConflictException("projects.project-not-open");
        }
        var accepted = pendingProposal(proposalId);
        accepted.accept();
        status = ProjectStatus.IN_PROGRESS;
        var now = Instant.now(clock);
        for (var other : proposals) {
            if (other != accepted && other.isPending()) {
                other.close();
                addDomainEvent(new ProposalDeclined(getId(), other.getId(), clientId, other.getStudentId(), title,
                        REASON_CLOSED, now));
            }
        }
        var agreedDeadline = LocalDate.now(clock).plusDays(accepted.getDeliveryDays());
        addDomainEvent(new ProposalAccepted(getId(), accepted.getId(), clientId, accepted.getStudentId(), title,
                category.name(), accepted.getPrice().amount(), accepted.getPrice().currency(),
                accepted.getDeliveryDays(), agreedDeadline, now));
        return accepted;
    }

    public Proposal declineProposal(UUID proposalId) {
        var proposal = pendingProposal(proposalId);
        proposal.decline();
        addDomainEvent(new ProposalDeclined(getId(), proposal.getId(), clientId, proposal.getStudentId(), title,
                REASON_DECLINED, Instant.now()));
        return proposal;
    }

    public Optional<Proposal> findProposal(UUID proposalId) {
        return proposals.stream().filter(proposal -> proposal.getId().equals(proposalId)).findFirst();
    }

    public boolean isOwnedBy(UUID userId) {
        return clientId.equals(userId);
    }

    private Proposal pendingProposal(UUID proposalId) {
        var proposal = findProposal(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("projects.proposal.not-found"));
        if (!proposal.isPending()) {
            throw new ConflictException("projects.proposal-not-pending");
        }
        return proposal;
    }

    private static List<String> clean(List<String> values) {
        if (values == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(values.stream().map(String::trim).filter(value -> !value.isEmpty()).distinct()
                .toList());
    }

    public UUID getClientId() {
        return clientId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ServiceCategory getCategory() {
        return category;
    }

    public List<String> getRequiredSkills() {
        return List.copyOf(requiredSkills);
    }

    public List<String> getDeliverables() {
        return List.copyOf(deliverables);
    }

    public Money getBudget() {
        return budget;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public boolean isAssisted() {
        return assisted;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public List<Proposal> getProposals() {
        return List.copyOf(proposals);
    }

    public long getVersion() {
        return version;
    }
}
