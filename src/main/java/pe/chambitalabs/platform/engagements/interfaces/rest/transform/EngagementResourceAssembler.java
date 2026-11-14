package pe.chambitalabs.platform.engagements.interfaces.rest.transform;

import pe.chambitalabs.platform.engagements.domain.model.aggregates.Engagement;
import pe.chambitalabs.platform.engagements.domain.model.entities.Deliverable;
import pe.chambitalabs.platform.engagements.interfaces.rest.resources.DeliverableResource;
import pe.chambitalabs.platform.engagements.interfaces.rest.resources.EngagementResource;

public final class EngagementResourceAssembler {

    private EngagementResourceAssembler() {
    }

    public static EngagementResource toResource(Engagement engagement) {
        return new EngagementResource(engagement.getId(), engagement.getProjectId(), engagement.getProposalId(),
                engagement.getClientId(), engagement.getStudentId(), engagement.getProjectTitle(),
                engagement.getCategory(), engagement.getAgreedPrice().amount(), engagement.getAgreedPrice().currency(),
                engagement.getDeliveryDays(), engagement.getDeadline(), engagement.getStatus().name(),
                engagement.getCreatedAt(), engagement.getActivatedAt(), engagement.getCompletedAt(),
                engagement.getDeliverables().stream().map(EngagementResourceAssembler::toResource).toList());
    }

    public static DeliverableResource toResource(Deliverable deliverable) {
        return new DeliverableResource(deliverable.getId(), deliverable.getFileUrl(), deliverable.getLinkUrl(),
                deliverable.getNote(), deliverable.getStatus().name(), deliverable.getSubmittedAt(),
                deliverable.getReviewedAt());
    }
}
