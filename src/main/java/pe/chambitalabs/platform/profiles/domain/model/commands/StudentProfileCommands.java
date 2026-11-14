package pe.chambitalabs.platform.profiles.domain.model.commands;

import pe.chambitalabs.platform.profiles.domain.model.valueobjects.RatingSummary;
import pe.chambitalabs.platform.profiles.domain.model.valueobjects.ServiceCategory;

import java.util.List;
import java.util.UUID;

/** Commands of the student profile. */
public final class StudentProfileCommands {

    private StudentProfileCommands() {
    }

    public record CreateStudentProfileCommand(UUID userId, String fullName, String university) {
    }

    public record UpdateStudentProfileCommand(UUID userId, String career, Integer cycle, String bio,
                                              List<String> skills, List<ServiceCategory> serviceCategories) {
    }

    public record AddPortfolioItemCommand(UUID userId, String title, String description, String imageUrl,
                                          String externalUrl) {
    }

    public record RemovePortfolioItemCommand(UUID userId, UUID itemId) {
    }

    public record UpdateRatingCommand(UUID userId, RatingSummary rating) {
    }

    public record RegisterCertificateCommand(UUID userId) {
    }
}
