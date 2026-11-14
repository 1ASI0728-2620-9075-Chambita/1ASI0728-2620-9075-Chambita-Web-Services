package pe.chambitalabs.platform.payments.domain;

import org.junit.jupiter.api.Test;
import pe.chambitalabs.platform.payments.domain.model.aggregates.Payment;
import pe.chambitalabs.platform.payments.domain.model.events.PaymentHeld;
import pe.chambitalabs.platform.payments.domain.model.events.PaymentReleased;
import pe.chambitalabs.platform.payments.domain.model.valueobjects.PaymentStatus;
import pe.chambitalabs.platform.payments.domain.services.ServiceFeePolicy;
import pe.chambitalabs.platform.shared.domain.exceptions.ConflictException;
import pe.chambitalabs.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private final Instant now = Instant.parse("2026-11-14T15:00:00Z");

    private Payment payment() {
        return Payment.pending(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                Money.soles(new BigDecimal("450.00")), new ServiceFeePolicy(new BigDecimal("0.10")));
    }

    @Test
    void chargesTheClientTheAmountPlusTheFeeAndPaysTheStudentTheAgreedAmount() {
        var payment = payment();
        assertThat(payment.getServiceFee().amount()).isEqualByComparingTo("45.00");
        assertThat(payment.totalCharged().amount()).isEqualByComparingTo("495.00");
        assertThat(payment.studentPayout().amount()).isEqualByComparingTo("450.00");
    }

    @Test
    void movesFromHeldToReleasedOnlyOnce() {
        var payment = payment();
        payment.confirm(now);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.HELD);
        assertThat(payment.release(now)).isTrue();
        assertThat(payment.release(now)).isFalse();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.RELEASED);
        assertThat(payment.domainEventsForTest()).hasAtLeastOneElementOfType(PaymentHeld.class)
                .filteredOn(PaymentReleased.class::isInstance).hasSize(1);
    }

    @Test
    void cannotBePaidTwiceNorReleasedBeforeBeingHeld() {
        var payment = payment();
        assertThatThrownBy(() -> payment.release(now)).isInstanceOf(ConflictException.class);
        payment.confirm(now);
        assertThatThrownBy(() -> payment.confirm(now)).isInstanceOf(ConflictException.class);
    }

    @Test
    void rejectsAnInvalidFeeRate() {
        assertThatThrownBy(() -> new ServiceFeePolicy(new BigDecimal("1.5"))).isInstanceOf(IllegalArgumentException.class);
    }
}
