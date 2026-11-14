package pe.chambitalabs.platform.payments.domain.model.commands;

import java.math.BigDecimal;
import java.util.UUID;

/** Commands of the Payments context. */
public final class PaymentCommands {

    private PaymentCommands() {
    }

    public record CreatePaymentCommand(UUID engagementId, UUID clientId, UUID studentId, BigDecimal amount,
                                       String currency) {
    }

    public record ConfirmPaymentCommand(UUID engagementId, UUID clientId) {
    }

    public record ReleasePaymentCommand(UUID engagementId) {
    }

    public record CancelPaymentCommand(UUID engagementId) {
    }
}
