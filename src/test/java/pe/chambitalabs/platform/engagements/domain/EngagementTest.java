package pe.chambitalabs.platform.engagements.domain;

import org.junit.jupiter.api.Test;
import pe.chambitalabs.platform.engagements.domain.model.aggregates.Engagement;
import pe.chambitalabs.platform.engagements.domain.model.events.DeliverableApproved;
import pe.chambitalabs.platform.engagements.domain.model.events.EngagementCompleted;
import pe.chambitalabs.platform.engagements.domain.model.events.EngagementCreated;
import pe.chambitalabs.platform.engagements.domain.model.valueobjects.EngagementStatus;
import pe.chambitalabs.platform.shared.domain.exceptions.BadRequestException;
import pe.chambitalabs.platform.shared.domain.exceptions.ConflictException;
import pe.chambitalabs.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EngagementTest {

    private final Instant now = Instant.parse("2026-11-14T15:00:00Z");

    private Engagement engagement() {
        return Engagement.fromAcceptedProposal(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "Identidad visual", "GRAPHIC_DESIGN", Money.soles(new BigDecimal("450")), 10,
                LocalDate.of(2026, 11, 24));
    }

    @Test
    void startsPendingPaymentAndPublishesEngagementCreated() {
        var engagement = engagement();
        assertThat(engagement.getStatus()).isEqualTo(EngagementStatus.PENDING_PAYMENT);
        assertThat(engagement.domainEventsForTest()).hasAtLeastOneElementOfType(EngagementCreated.class);
    }

    @Test
    void followsTheHappyPathUntilCompleted() {
        var engagement = engagement();
        engagement.activate(now);
        engagement.submitDeliverable(null, "https://drive.example/work", "Final", now);
        assertThat(engagement.getStatus()).isEqualTo(EngagementStatus.IN_REVIEW);
        engagement.approveDeliverable(now, false);
        engagement.complete(now);
        assertThat(engagement.getStatus()).isEqualTo(EngagementStatus.COMPLETED);
        assertThat(engagement.domainEventsForTest()).hasAtLeastOneElementOfType(DeliverableApproved.class)
                .hasAtLeastOneElementOfType(EngagementCompleted.class);
    }

    @Test
    void rejectsAnEmptyDeliverableAndDeliveringBeforePayment() {
        var engagement = engagement();
        assertThatThrownBy(() -> engagement.submitDeliverable(null, "https://x.example", null, now))
                .isInstanceOf(ConflictException.class);
        engagement.activate(now);
        assertThatThrownBy(() -> engagement.submitDeliverable(" ", null, null, now))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void rejectsASecondApproval() {
        var engagement = engagement();
        engagement.activate(now);
        engagement.submitDeliverable(null, "https://drive.example/work", null, now);
        engagement.approveDeliverable(now, false);
        assertThatThrownBy(() -> engagement.approveDeliverable(now, false)).isInstanceOf(ConflictException.class);
    }

    @Test
    void approvesAutomaticallyAfterTheReviewPeriod() {
        var engagement = engagement();
        engagement.activate(now);
        engagement.submitDeliverable(null, "https://drive.example/work", null, now);
        assertThat(engagement.autoApproveIfOverdue(now.plus(Duration.ofDays(6)), Duration.ofDays(7))).isFalse();
        assertThat(engagement.autoApproveIfOverdue(now.plus(Duration.ofDays(7)).plusSeconds(1), Duration.ofDays(7)))
                .isTrue();
        var event = engagement.domainEventsForTest().stream().filter(DeliverableApproved.class::isInstance)
                .map(DeliverableApproved.class::cast).findFirst().orElseThrow();
        assertThat(event.automatic()).isTrue();
    }

    @Test
    void canOnlyBeCancelledBeforePayment() {
        var engagement = engagement();
        engagement.activate(now);
        assertThatThrownBy(() -> engagement.cancel(now)).isInstanceOf(ConflictException.class);
    }
}
