package pe.chambitalabs.platform.iam.domain.services;

import pe.chambitalabs.platform.iam.domain.model.commands.ConfirmStudentVerificationCommand;
import pe.chambitalabs.platform.iam.domain.model.commands.RequestStudentVerificationCommand;

/** Use cases of the institutional email verification (TS02). */
public interface StudentVerificationCommandService {

    void handle(RequestStudentVerificationCommand command);

    void handle(ConfirmStudentVerificationCommand command);
}
