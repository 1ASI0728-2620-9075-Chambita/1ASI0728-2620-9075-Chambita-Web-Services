package pe.chambitalabs.platform.payments.application.internal.eventhandlers;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import pe.chambitalabs.platform.engagements.domain.model.events.DeliverableApproved;
import pe.chambitalabs.platform.engagements.domain.model.events.EngagementCancelled;
import pe.chambitalabs.platform.engagements.domain.model.events.EngagementCreated;
import pe.chambitalabs.platform.payments.application.internal.commandservices.PaymentCommandService;
import pe.chambitalabs.platform.payments.domain.model.commands.PaymentCommands.CancelPaymentCommand;
import pe.chambitalabs.platform.payments.domain.model.commands.PaymentCommands.CreatePaymentCommand;
import pe.chambitalabs.platform.payments.domain.model.commands.PaymentCommands.ReleasePaymentCommand;

/** Partnership with Engagements: pending payment, release on approval and cancellation. */
@Component
public class EngagementsEventsHandler {

    private final PaymentCommandService commandService;

    public EngagementsEventsHandler(PaymentCommandService commandService) {
        this.commandService = commandService;
    }

    @ApplicationModuleListener
    public void on(EngagementCreated event) {
        commandService.handle(new CreatePaymentCommand(event.engagementId(), event.clientId(), event.studentId(),
                event.amount(), event.currency()));
    }

    @ApplicationModuleListener
    public void on(DeliverableApproved event) {
        commandService.handle(new ReleasePaymentCommand(event.engagementId()));
    }

    @ApplicationModuleListener
    public void on(EngagementCancelled event) {
        commandService.handle(new CancelPaymentCommand(event.engagementId()));
    }
}
