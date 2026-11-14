package pe.chambitalabs.platform.projects.domain.services;

import pe.chambitalabs.platform.projects.domain.model.aggregates.Project;
import pe.chambitalabs.platform.projects.domain.model.commands.ProjectCommands.AcceptProposalCommand;
import pe.chambitalabs.platform.projects.domain.model.commands.ProjectCommands.DeclineProposalCommand;
import pe.chambitalabs.platform.projects.domain.model.commands.ProjectCommands.PublishProjectCommand;
import pe.chambitalabs.platform.projects.domain.model.commands.ProjectCommands.SubmitProposalCommand;
import pe.chambitalabs.platform.projects.domain.model.entities.Proposal;

public interface ProjectCommandService {

    Project handle(PublishProjectCommand command);

    Proposal handle(SubmitProposalCommand command);

    Proposal handle(AcceptProposalCommand command);

    Proposal handle(DeclineProposalCommand command);
}
