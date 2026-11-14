package pe.chambitalabs.platform.shared.domain;

import org.junit.jupiter.api.Test;
import pe.chambitalabs.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void keepsTwoDecimalsAndAddsAmountsOfTheSameCurrency() {
        var total = Money.soles(new BigDecimal("450")).add(Money.soles(new BigDecimal("45.005")));
        assertThat(total.amount()).isEqualByComparingTo("495.01");
        assertThat(total.currency()).isEqualTo("PEN");
    }

    @Test
    void rejectsNegativeAmountsAndDifferentCurrencies() {
        assertThatThrownBy(() -> Money.soles(new BigDecimal("-1"))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Money.soles(BigDecimal.ONE).add(new Money(BigDecimal.ONE, "USD")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
