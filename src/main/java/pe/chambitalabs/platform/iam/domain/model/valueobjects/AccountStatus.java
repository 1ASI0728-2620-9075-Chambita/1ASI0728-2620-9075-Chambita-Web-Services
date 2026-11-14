package pe.chambitalabs.platform.iam.domain.model.valueobjects;

/** Status of a user account. Students stay pending until their institutional email is verified. */
public enum AccountStatus { PENDING_VERIFICATION, ACTIVE, SUSPENDED }
