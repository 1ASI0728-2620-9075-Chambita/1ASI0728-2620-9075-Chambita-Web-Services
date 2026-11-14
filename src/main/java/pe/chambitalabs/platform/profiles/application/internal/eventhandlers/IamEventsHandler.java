package pe.chambitalabs.platform.profiles.application.internal.eventhandlers;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import pe.chambitalabs.platform.iam.domain.model.events.ClientAccountRegistered;
import pe.chambitalabs.platform.iam.domain.model.events.StudentVerified;
import pe.chambitalabs.platform.profiles.domain.model.commands.ClientProfileCommands.CreateClientProfileCommand;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.CreateStudentProfileCommand;
import pe.chambitalabs.platform.profiles.domain.services.ClientProfileCommandService;
import pe.chambitalabs.platform.profiles.domain.services.StudentProfileCommandService;

/** Creates the student profile when a student is verified and the client profile at registration. */
@Component
public class IamEventsHandler {

    private final StudentProfileCommandService studentProfileCommandService;
    private final ClientProfileCommandService clientProfileCommandService;

    public IamEventsHandler(StudentProfileCommandService studentProfileCommandService,
                            ClientProfileCommandService clientProfileCommandService) {
        this.studentProfileCommandService = studentProfileCommandService;
        this.clientProfileCommandService = clientProfileCommandService;
    }

    @ApplicationModuleListener
    public void on(StudentVerified event) {
        studentProfileCommandService.handle(new CreateStudentProfileCommand(event.userId(), event.fullName(),
                event.university()));
    }

    @ApplicationModuleListener
    public void on(ClientAccountRegistered event) {
        clientProfileCommandService.handle(new CreateClientProfileCommand(event.userId(), event.fullName()));
    }
}
