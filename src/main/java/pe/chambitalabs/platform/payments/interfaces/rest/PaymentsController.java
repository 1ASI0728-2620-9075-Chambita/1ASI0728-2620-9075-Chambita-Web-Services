package pe.chambitalabs.platform.payments.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.chambitalabs.platform.payments.application.internal.commandservices.PaymentCommandService;
import pe.chambitalabs.platform.payments.domain.model.aggregates.Payment;
import pe.chambitalabs.platform.payments.domain.model.commands.PaymentCommands.ConfirmPaymentCommand;
import pe.chambitalabs.platform.payments.infrastructure.persistence.jpa.repositories.PaymentRepository;
import pe.chambitalabs.platform.payments.interfaces.rest.resources.PaymentResource;
import pe.chambitalabs.platform.shared.domain.exceptions.ForbiddenException;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;
import pe.chambitalabs.platform.shared.infrastructure.security.AuthenticatedUser;

import java.util.List;
import java.util.UUID;

/** Simulated protected payment: confirmation and queries (TS08, US27, US29). */
@RestController
@Tag(name = "Payments", description = "Protected (simulated) payments")
public class PaymentsController {

    private final PaymentCommandService commandService;
    private final PaymentRepository repository;

    public PaymentsController(PaymentCommandService commandService, PaymentRepository repository) {
        this.commandService = commandService;
        this.repository = repository;
    }

    @PostMapping("/api/v1/engagements/{engagementId}/payments")
    @Operation(summary = "Confirms the simulated payment; the money is held and the engagement becomes ACTIVE")
    public PaymentResource pay(@PathVariable UUID engagementId, @AuthenticationPrincipal AuthenticatedUser principal) {
        return toResource(commandService.handle(new ConfirmPaymentCommand(engagementId, principal.id())));
    }

    @GetMapping("/api/v1/engagements/{engagementId}/payments")
    @Operation(summary = "Returns the payment of an engagement")
    public PaymentResource byEngagement(@PathVariable UUID engagementId,
                                        @AuthenticationPrincipal AuthenticatedUser principal) {
        var payment = repository.findByEngagementId(engagementId)
                .orElseThrow(() -> new ResourceNotFoundException("payments.payment.not-found"));
        if (!payment.getClientId().equals(principal.id()) && !payment.getStudentId().equals(principal.id())) {
            throw new ForbiddenException("engagements.participant-required");
        }
        return toResource(payment);
    }

    @GetMapping("/api/v1/students/{studentId}/payments")
    @Operation(summary = "Lists the held and released payments of the authenticated student")
    public List<PaymentResource> byStudent(@PathVariable UUID studentId,
                                           @AuthenticationPrincipal AuthenticatedUser principal) {
        if (!studentId.equals(principal.id())) {
            throw new ForbiddenException("error.forbidden");
        }
        return repository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(PaymentsController::toResource).toList();
    }

    private static PaymentResource toResource(Payment payment) {
        return new PaymentResource(payment.getId(), payment.getEngagementId(), payment.getClientId(),
                payment.getStudentId(), payment.getAmount().amount(), payment.getServiceFee().amount(),
                payment.totalCharged().amount(), payment.studentPayout().amount(), payment.getAmount().currency(),
                payment.getStatus().name(), payment.getHeldAt(), payment.getReleasedAt());
    }
}
