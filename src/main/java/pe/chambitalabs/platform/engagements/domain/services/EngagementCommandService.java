package pe.chambitalabs.platform.engagements.domain.services;

import pe.chambitalabs.platform.engagements.domain.model.aggregates.Engagement;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.ActivateEngagementCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.ApproveDeliverableCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.AutoApproveOverdueDeliverablesCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.CancelEngagementCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.CompleteEngagementCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.CreateEngagementCommand;
import pe.chambitalabs.platform.engagements.domain.model.commands.EngagementCommands.SubmitDeliverableCommand;

public interface EngagementCommandService {

    Engagement handle(CreateEngagementCommand command);

    void handle(ActivateEngagementCommand command);

    Engagement handle(SubmitDeliverableCommand command);

    Engagement handle(ApproveDeliverableCommand command);

    int handle(AutoApproveOverdueDeliverablesCommand command);

    void handle(CompleteEngagementCommand command);

    Engagement handle(CancelEngagementCommand command);
}
