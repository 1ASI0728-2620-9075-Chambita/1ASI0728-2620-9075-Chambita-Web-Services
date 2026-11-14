CREATE TABLE reputation.reviewable_engagements (
    id               UUID PRIMARY KEY,
    engagement_id    UUID         NOT NULL UNIQUE,
    client_id        UUID         NOT NULL,
    student_id       UUID         NOT NULL,
    project_title    VARCHAR(120) NOT NULL,
    category         VARCHAR(30)  NOT NULL,
    completed_at     TIMESTAMPTZ  NOT NULL,
    client_reviewed  BOOLEAN      NOT NULL DEFAULT FALSE,
    student_reviewed BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL
);

CREATE TABLE reputation.reviews (
    id            UUID PRIMARY KEY,
    engagement_id UUID        NOT NULL,
    reviewer_id   UUID        NOT NULL,
    reviewee_id   UUID        NOT NULL,
    reviewer_role VARCHAR(20) NOT NULL,
    score         INT         NOT NULL CHECK (score BETWEEN 1 AND 5),
    comment       VARCHAR(600),
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    UNIQUE (engagement_id, reviewer_id)
);
CREATE INDEX idx_reviews_reviewee ON reputation.reviews (reviewee_id);

CREATE TABLE reputation.reputation_summaries (
    id            UUID PRIMARY KEY,
    user_id       UUID          NOT NULL UNIQUE,
    average_score NUMERIC(3, 2) NOT NULL DEFAULT 0,
    review_count  INT           NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ   NOT NULL,
    updated_at    TIMESTAMPTZ   NOT NULL
);
