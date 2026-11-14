package pe.chambitalabs.platform.reputation.application.internal.eventhandlers;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import pe.chambitalabs.platform.engagements.domain.model.events.EngagementCompleted;
import pe.chambitalabs.platform.reputation.application.internal.commandservices.ReviewCommandService;
import pe.chambitalabs.platform.reputation.domain.model.commands.ReviewCommands.RegisterReviewableEngagementCommand;

/** Enables the reviews of a completed engagement. */
@Component
public class EngagementCompletedEventHandler {

    private final ReviewCommandService commandService;

    public EngagementCompletedEventHandler(ReviewCommandService commandService) {
        this.commandService = commandService;
    }

    @ApplicationModuleListener
    public void on(EngagementCompleted event) {
        commandService.handle(new RegisterReviewableEngagementCommand(event.engagementId(), event.clientId(),
                event.studentId(), event.projectTitle(), event.category(), event.occurredOn()));
    }
}
