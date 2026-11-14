package pe.chambitalabs.platform.projects.domain.model.valueobjects;

/** Status of a proposal. CLOSED means another proposal was accepted or the project was cancelled. */
public enum ProposalStatus { PENDING, ACCEPTED, DECLINED, CLOSED }
