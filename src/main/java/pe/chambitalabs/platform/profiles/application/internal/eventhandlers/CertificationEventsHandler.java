package pe.chambitalabs.platform.profiles.application.internal.eventhandlers;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import pe.chambitalabs.platform.certification.domain.model.events.CertificateIssued;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.RegisterCertificateCommand;
import pe.chambitalabs.platform.profiles.domain.services.StudentProfileCommandService;

/** Increases the certificate count of the student. */
@Component
public class CertificationEventsHandler {

    private final StudentProfileCommandService commandService;

    public CertificationEventsHandler(StudentProfileCommandService commandService) {
        this.commandService = commandService;
    }

    @ApplicationModuleListener
    public void on(CertificateIssued event) {
        commandService.handle(new RegisterCertificateCommand(event.studentId()));
    }
}
