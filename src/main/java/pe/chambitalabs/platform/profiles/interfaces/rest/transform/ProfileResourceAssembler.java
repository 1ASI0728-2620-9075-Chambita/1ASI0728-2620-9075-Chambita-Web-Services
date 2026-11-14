package pe.chambitalabs.platform.profiles.interfaces.rest.transform;

import pe.chambitalabs.platform.profiles.domain.model.aggregates.ClientProfile;
import pe.chambitalabs.platform.profiles.domain.model.aggregates.StudentProfile;
import pe.chambitalabs.platform.profiles.domain.model.entities.PortfolioItem;
import pe.chambitalabs.platform.profiles.interfaces.rest.resources.ClientProfileResource;
import pe.chambitalabs.platform.profiles.interfaces.rest.resources.PortfolioItemResource;
import pe.chambitalabs.platform.profiles.interfaces.rest.resources.StudentProfileResource;

import java.util.LinkedHashSet;

public final class ProfileResourceAssembler {

    private ProfileResourceAssembler() {
    }

    public static StudentProfileResource toResource(StudentProfile profile) {
        var categories = new LinkedHashSet<String>();
        profile.getServiceCategories().forEach(category -> categories.add(category.name()));
        return new StudentProfileResource(profile.getId(), profile.getUserId(), profile.getFullName(),
                profile.getUniversity(), profile.getCareer(), profile.getCycle(), profile.getBio(), profile.getSkills(),
                categories, profile.getRating().average(), profile.getRating().count(), profile.getRating().isNew(),
                profile.getCertificateCount(), profile.isComplete(),
                profile.getPortfolioItems().stream().map(ProfileResourceAssembler::toResource).toList());
    }

    public static PortfolioItemResource toResource(PortfolioItem item) {
        return new PortfolioItemResource(item.getId(), item.getTitle(), item.getDescription(), item.getImageUrl(),
                item.getExternalUrl(), item.getCreatedAt());
    }

    public static ClientProfileResource toResource(ClientProfile profile) {
        return new ClientProfileResource(profile.getId(), profile.getUserId(), profile.getDisplayName(),
                profile.getBusinessName(), profile.getDistrict(), profile.getRating().average(),
                profile.getRating().count());
    }
}
