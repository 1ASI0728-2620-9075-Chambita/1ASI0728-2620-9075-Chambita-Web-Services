package pe.chambitalabs.platform.profiles.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.chambitalabs.platform.profiles.domain.model.aggregates.StudentProfile;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.AddPortfolioItemCommand;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.CreateStudentProfileCommand;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.RegisterCertificateCommand;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.RemovePortfolioItemCommand;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.UpdateRatingCommand;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.UpdateStudentProfileCommand;
import pe.chambitalabs.platform.profiles.domain.model.entities.PortfolioItem;
import pe.chambitalabs.platform.profiles.domain.services.StudentProfileCommandService;
import pe.chambitalabs.platform.profiles.infrastructure.persistence.jpa.repositories.StudentProfileRepository;
import pe.chambitalabs.platform.shared.domain.exceptions.ResourceNotFoundException;

import java.util.List;

/** Creates and updates student profiles and portfolios (TS03, US13, US14). */
@Service
@Transactional
public class StudentProfileCommandServiceImpl implements StudentProfileCommandService {

    private final StudentProfileRepository repository;

    public StudentProfileCommandServiceImpl(StudentProfileRepository repository) {
        this.repository = repository;
    }

    @Override
    public StudentProfile handle(CreateStudentProfileCommand command) {
        return repository.findByUserId(command.userId())
                .orElseGet(() -> repository.save(new StudentProfile(command.userId(), command.fullName(),
                        command.university())));
    }

    @Override
    public StudentProfile handle(UpdateStudentProfileCommand command) {
        var profile = find(command);
        profile.update(command.career(), command.cycle(), command.bio(),
                command.skills() == null ? List.of() : command.skills(),
                command.serviceCategories() == null ? List.of() : command.serviceCategories());
        return repository.save(profile);
    }

    @Override
    public PortfolioItem handle(AddPortfolioItemCommand command) {
        var profile = repository.findByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("profiles.student.not-found"));
        var item = profile.addPortfolioItem(command.title(), command.description(), command.imageUrl(),
                command.externalUrl());
        repository.save(profile);
        return item;
    }

    @Override
    public void handle(RemovePortfolioItemCommand command) {
        var profile = repository.findByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("profiles.student.not-found"));
        if (!profile.removePortfolioItem(command.itemId())) {
            throw new ResourceNotFoundException("profiles.portfolio-item.not-found");
        }
        repository.save(profile);
    }

    @Override
    public void handle(UpdateRatingCommand command) {
        repository.findByUserId(command.userId()).ifPresent(profile -> {
            profile.updateRating(command.rating());
            repository.save(profile);
        });
    }

    @Override
    public void handle(RegisterCertificateCommand command) {
        repository.findByUserId(command.userId()).ifPresent(profile -> {
            profile.registerCertificate();
            repository.save(profile);
        });
    }

    private StudentProfile find(UpdateStudentProfileCommand command) {
        return repository.findByUserId(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("profiles.student.not-found"));
    }
}
