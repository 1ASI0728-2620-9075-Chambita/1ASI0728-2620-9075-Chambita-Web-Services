package pe.chambitalabs.platform.iam.domain.model.commands;

public record ConfirmStudentVerificationCommand(String email, String code) {
}
