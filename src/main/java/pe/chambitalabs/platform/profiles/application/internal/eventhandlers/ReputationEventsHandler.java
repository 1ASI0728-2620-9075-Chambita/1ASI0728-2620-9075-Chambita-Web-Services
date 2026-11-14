package pe.chambitalabs.platform.profiles.application.internal.eventhandlers;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import pe.chambitalabs.platform.profiles.domain.model.commands.StudentProfileCommands.UpdateRatingCommand;
import pe.chambitalabs.platform.profiles.domain.model.valueobjects.RatingSummary;
import pe.chambitalabs.platform.profiles.domain.services.StudentProfileCommandService;
import pe.chambitalabs.platform.profiles.infrastructure.persistence.jpa.repositories.ClientProfileRepository;
import pe.chambitalabs.platform.reputation.domain.model.events.RatingUpdated;

/** Keeps the local copy of the reputation of students and clients. */
@Component
public class ReputationEventsHandler {

    private final StudentProfileCommandService studentProfileCommandService;
    private final ClientProfileRepository clientProfileRepository;

    public ReputationEventsHandler(StudentProfileCommandService studentProfileCommandService,
                                   ClientProfileRepository clientProfileRepository) {
        this.studentProfileCommandService = studentProfileCommandService;
        this.clientProfileRepository = clientProfileRepository;
    }

    @ApplicationModuleListener
    public void on(RatingUpdated event) {
        var rating = new RatingSummary(event.average(), event.count());
        studentProfileCommandService.handle(new UpdateRatingCommand(event.userId(), rating));
        clientProfileRepository.findByUserId(event.userId()).ifPresent(profile -> {
            profile.updateRating(rating);
            clientProfileRepository.save(profile);
        });
    }
}
