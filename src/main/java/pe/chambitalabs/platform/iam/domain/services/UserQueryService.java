package pe.chambitalabs.platform.iam.domain.services;

import pe.chambitalabs.platform.iam.domain.model.aggregates.User;
import pe.chambitalabs.platform.iam.domain.model.queries.GetUserByIdQuery;

import java.util.Optional;

public interface UserQueryService {

    Optional<User> handle(GetUserByIdQuery query);
}
