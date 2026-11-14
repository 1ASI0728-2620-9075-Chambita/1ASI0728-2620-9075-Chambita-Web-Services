package pe.chambitalabs.platform.engagements.application.internal.eventhandlers;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.ActivateEngagementCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.CompleteEngagementCommand;
import pe.chambitalabs.platform.engagements.domain.services.EngagementCommandService;
import pe.chambitalabs.platform.payments.domain.model.events.PaymentHeld;
import pe.chambitalabs.platform.payments.domain.model.events.PaymentReleased;

/** Partnership with Payments: activates the engagement when paid and completes it when released. */
@Component
public class PaymentsEventsHandler {

    private final EngagementCommandService commandService;

    public PaymentsEventsHandler(EngagementCommandService commandService) {
        this.commandService = commandService;
    }

    @ApplicationModuleListener
    public void on(PaymentHeld event) {
        commandService.handle(new ActivateEngagementCommand(event.engagementId()));
    }

    @ApplicationModuleListener
    public void on(PaymentReleased event) {
        commandService.handle(new CompleteEngagementCommand(event.engagementId()));
    }
}
