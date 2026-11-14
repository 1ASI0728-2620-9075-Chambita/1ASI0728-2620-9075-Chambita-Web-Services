package pe.chambitalabs.platform.payments.application.internal.commandservices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.payments.domain.model.aggregates.Payment;
import pe.chambitalabs.platform.payments.domain.model.commands.PaymentCommands.CancelPaymentCommand;
import pe.chambitalabs.platform.payments.domain.model.commands.PaymentCommands.ConfirmPaymentCommand;
import pe.chambitalabs.platform.payments.domain.model.commands.PaymentCommands.CreatePaymentCommand;
import pe.chambitalabs.platform.payments.domain.model.commands.PaymentCommands.ReleasePaymentCommand;
import pe.chambitalabs.platform.payments.domain.services.ServiceFeePolicy;
import pe.chambitalabs.platform.payments.infrastructure.persistence.jpa.repositories.PaymentRepository;
import pe.chambitalabs.platform.shared.domain.exceptions.ForbiddenException;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;
import pe.chambitalabs.platform.shared.domain.model.valueobjects.Money;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;

/** Creates, confirms, releases and cancels simulated payments (TS08, US27, US28). */
@Service
@Transactional
public class PaymentCommandService {

    private final PaymentRepository repository;
    private final ServiceFeePolicy feePolicy;
    private final Clock clock;

    public PaymentCommandService(PaymentRepository repository, Clock clock,
                                 @Value("${chambita.payments.service-fee-rate:0.10}") BigDecimal serviceFeeRate) {
        this.repository = repository;
        this.clock = clock;
        this.feePolicy = new ServiceFeePolicy(serviceFeeRate);
    }

    public Payment handle(CreatePaymentCommand command) {
        return repository.findByEngagementId(command.engagementId()).orElseGet(() -> repository.save(
                Payment.pending(command.engagementId(), command.clientId(), command.studentId(),
                        new Money(command.amount(), command.currency()), feePolicy)));
    }

    public Payment handle(ConfirmPaymentCommand command) {
        var payment = find(command.engagementId());
        if (!payment.getClientId().equals(command.clientId())) {
            throw new ForbiddenException("payments.client-required");
        }
        payment.confirm(Instant.now(clock));
        return repository.saveAndFlush(payment);
    }

    public void handle(ReleasePaymentCommand command) {
        var payment = find(command.engagementId());
        if (payment.release(Instant.now(clock))) {
            repository.save(payment);
        }
    }

    public void handle(CancelPaymentCommand command) {
        repository.findByEngagementId(command.engagementId()).ifPresent(payment -> {
            payment.cancel();
            repository.save(payment);
        });
    }

    private Payment find(java.util.UUID engagementId) {
        return repository.findByEngagementId(engagementId)
                .orElseThrow(() -> new ResourceNotFoundException("payments.payment.not-found"));
    }
}
