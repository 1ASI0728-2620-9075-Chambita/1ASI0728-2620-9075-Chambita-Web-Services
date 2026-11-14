package pe.chambitalabs.platform.profiles.domain.services;

import pe.chambitalabs.platform.profiles.domain.model.aggregates.StudentProfile;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.AddPortfolioItemCommand;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.CreateStudentProfileCommand;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.RegisterCertificateCommand;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.RemovePortfolioItemCommand;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.UpdateRatingCommand;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.UpdateStudentProfileCommand;
import pe.chambitalabs.platform.profiles.domain.model.entities.PortfolioItem;

public interface StudentProfileCommandService {

    StudentProfile handle(CreateStudentProfileCommand command);

    StudentProfile handle(UpdateStudentProfileCommand command);

    PortfolioItem handle(AddPortfolioItemCommand command);

    void handle(RemovePortfolioItemCommand command);

    void handle(UpdateRatingCommand command);

    void handle(RegisterCertificateCommand command);
}
