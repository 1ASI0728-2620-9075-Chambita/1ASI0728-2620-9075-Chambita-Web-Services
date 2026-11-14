package pe.chambitalabs.platform.payments.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResource(UUID id, UUID engagementId, UUID clientId, UUID studentId, BigDecimal amount,
                              BigDecimal serviceFee, BigDecimal totalCharged, BigDecimal studentPayout,
                              String currency, String status, Instant heldAt, Instant releasedAt) {
}
