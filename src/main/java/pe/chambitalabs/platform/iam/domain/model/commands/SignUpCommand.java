package pe.chambitalabs.platform.iam.domain.model.commands;

import pe.chambitalabs.platform.iam.domain.model.valueobjects.Role;

public record SignUpCommand(String email, String password, String fullName, Role role) {
}
