package pe.chambitalabs.platform.engagements.domain.model.queries;

import pe.chambitalabs.platform.engagements.domain.model.valueobjects.EngagementStatus;

import java.util.UUID;

/** Queries of the Engagements context. */
public final class EngagementQueries {

    private EngagementQueries() {
    }

    public record GetEngagementByIdQuery(UUID engagementId, UUID requesterId) {
    }

    public record GetEngagementsByParticipantQuery(UUID participantId, EngagementStatus status) {
    }
}
