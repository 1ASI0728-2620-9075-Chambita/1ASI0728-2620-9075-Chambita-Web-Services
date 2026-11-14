package pe.chambitalabs.platform.payments.domain.services;

import pe.chambitalabs.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;

/** Calculates the service fee with a configurable rate (QA08: a change stays inside Payments). */
public record ServiceFeePolicy(BigDecimal rate) {

    public ServiceFeePolicy {
        if (rate == null || rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) >= 0) {
            throw new IllegalArgumentException("Service fee rate must be between 0 and 1");
        }
    }

    public Money calculateFee(Money amount) {
        return amount.multiply(rate);
    }
}
