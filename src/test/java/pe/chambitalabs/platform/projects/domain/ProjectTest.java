package pe.chambitalabs.platform.projects.domain;

import org.junit.jupiter.api.Test;
import pe.chambitalabs.platform.projects.domain.model.aggregates.Project;
import pe.chambitalabs.platform.projects.domain.model.events.ProposalAccepted;
import pe.chambitalabs.platform.projects.domain.model.events.ProposalDeclined;
import pe.chambitalabs.platform.projects.domain.model.valueobjects.ProjectStatus;
import pe.chambitalabs.platform.projects.domain.model.valueobjects.ProposalStatus;
import pe.chambitalabs.platform.projects.domain.model.valueobjects.ServiceCategory;
import pe.chambitalabs.platform.shared.domain.exceptions.ConflictException;
import pe.chambitalabs.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-14T15:00:00Z"), ZoneId.of("America/Lima"));
    private final UUID clientId = UUID.randomUUID();

    private Project project() {
        return Project.publish(clientId, "Identidad visual para marca de ropa", "Logo, paleta y plantillas",
                ServiceCategory.GRAPHIC_DESIGN, List.of("Illustrator", " Canva ", "Canva"), List.of("Logo"),
                Money.soles(new BigDecimal("450")), LocalDate.of(2026, 12, 1), false);
    }

    @Test
    void publishesAnOpenProjectWithCleanSkills() {
        var project = project();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.OPEN);
        assertThat(project.getRequiredSkills()).containsExactly("Illustrator", "Canva");
    }

    @Test
    void rejectsADuplicatedProposalOfTheSameStudent() {
        var project = project();
        var studentId = UUID.randomUUID();
        project.submitProposal(studentId, Money.soles(new BigDecimal("450")), 10, "Hola");
        assertThatThrownBy(() -> project.submitProposal(studentId, Money.soles(new BigDecimal("400")), 8, "Otra"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void acceptingAProposalClosesTheOthersAndPublishesTheEvents() {
        var project = project();
        var accepted = project.submitProposal(UUID.randomUUID(), Money.soles(new BigDecimal("450")), 10, "A");
        var other = project.submitProposal(UUID.randomUUID(), Money.soles(new BigDecimal("420")), 12, "B");

        project.acceptProposal(accepted.getId(), clock);

        assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
        assertThat(accepted.getStatus()).isEqualTo(ProposalStatus.ACCEPTED);
        assertThat(other.getStatus()).isEqualTo(ProposalStatus.CLOSED);
        assertThat(project.domainEventsForTest()).hasAtLeastOneElementOfType(ProposalAccepted.class)
                .hasAtLeastOneElementOfType(ProposalDeclined.class);
        var event = project.domainEventsForTest().stream().filter(ProposalAccepted.class::isInstance)
                .map(ProposalAccepted.class::cast).findFirst().orElseThrow();
        assertThat(event.deadline()).isEqualTo(LocalDate.of(2026, 11, 24));
    }

    @Test
    void rejectsProposalsWhenTheProjectIsNoLongerOpen() {
        var project = project();
        var accepted = project.submitProposal(UUID.randomUUID(), Money.soles(new BigDecimal("450")), 10, "A");
        project.acceptProposal(accepted.getId(), clock);
        assertThatThrownBy(() -> project.submitProposal(UUID.randomUUID(), Money.soles(BigDecimal.TEN), 3, "Late"))
                .isInstanceOf(ConflictException.class);
    }
}
