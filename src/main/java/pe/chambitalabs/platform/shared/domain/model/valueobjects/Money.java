package pe.chambitalabs.platform.shared.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Amount with currency, used for budgets, prices and payments. Amounts keep two decimals.
 */
@Embeddable
public record Money(
        @Column(name = "amount", nullable = false, precision = 10, scale = 2) BigDecimal amount,
        @Column(name = "currency", nullable = false, length = 3) String currency) {

    public static final String PEN = "PEN";

    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Money amount cannot be negative");
        }
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money soles(BigDecimal amount) {
        return new Money(amount, PEN);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }

    public Money multiply(BigDecimal rate) {
        return new Money(amount.multiply(rate), currency);
    }

    public boolean isGreaterThan(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount) > 0;
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currencies differ: " + currency + " and " + other.currency);
        }
    }
}
