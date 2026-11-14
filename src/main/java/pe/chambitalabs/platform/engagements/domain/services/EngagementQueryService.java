package pe.chambitalabs.platform.engagements.domain.services;

import pe.chambitalabs.platform.engagements.domain.model.aggregates.Engagement;
import pe.chambitalabs.platform.engagements.domain.model.queries.EngagementQueries.GetEngagementByIdQuery;
import pe.chambitalabs.platform.engagements.domain.model.queries.EngagementQueries.GetEngagementsByParticipantQuery;

import java.util.List;

public interface EngagementQueryService {

    Engagement handle(GetEngagementByIdQuery query);

    List<Engagement> handle(GetEngagementsByParticipantQuery query);
}
