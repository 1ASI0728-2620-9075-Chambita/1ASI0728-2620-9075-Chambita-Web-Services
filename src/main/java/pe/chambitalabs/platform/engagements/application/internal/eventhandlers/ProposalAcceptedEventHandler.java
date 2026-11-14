package pe.chambitalabs.platform.engagements.application.internal.eventhandlers;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.CreateEngagementCommand;
import pe.chambitalabs.platform.engagements.domain.services.EngagementCommandService;
import pe.chambitalabs.platform.projects.domain.model.events.ProposalAccepted;

/** Creates the engagement pending payment when a proposal is accepted (TS06). */
@Component
public class ProposalAcceptedEventHandler {

    private final EngagementCommandService commandService;

    public ProposalAcceptedEventHandler(EngagementCommandService commandService) {
        this.commandService = commandService;
    }

    @ApplicationModuleListener
    public void on(ProposalAccepted event) {
        commandService.handle(new CreateEngagementCommand(event.projectId(), event.proposalId(), event.clientId(),
                event.studentId(), event.projectTitle(), event.category(), event.price(), event.currency(),
                event.deliveryDays(), event.deadline()));
    }
}
