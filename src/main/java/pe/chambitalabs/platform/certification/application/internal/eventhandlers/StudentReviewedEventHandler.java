package pe.chambitalabs.platform.certification.application.internal.eventhandlers;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import pe.chambitalabs.platform.certification.application.internal.commandservices.CertificateCommandService;
import pe.chambitalabs.platform.certification.domain.model.commands.CertificateCommands.IssueCertificateCommand;
import pe.chambitalabs.platform.reputation.domain.model.events.StudentReviewed;

/**
 * Starts the issuance when the client rates the student. The listener is asynchronous and keeps
 * its own short transactions while it waits for the network; the Event Publication Registry
 * keeps the event until it finishes (QA03).
 */
@Component
public class StudentReviewedEventHandler {

    private final CertificateCommandService commandService;

    public StudentReviewedEventHandler(CertificateCommandService commandService) {
        this.commandService = commandService;
    }

    @Async
    @TransactionalEventListener
    public void on(StudentReviewed event) {
        commandService.handle(new IssueCertificateCommand(event.engagementId(), event.studentId(),
                event.projectTitle(), event.category(), event.score(), event.occurredOn()));
    }
}
