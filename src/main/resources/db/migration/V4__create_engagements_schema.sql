CREATE TABLE engagements.engagements (
    id                    UUID PRIMARY KEY,
    project_id            UUID           NOT NULL,
    proposal_id           UUID           NOT NULL UNIQUE,
    client_id             UUID           NOT NULL,
    student_id            UUID           NOT NULL,
    project_title         VARCHAR(120)   NOT NULL,
    category              VARCHAR(30)    NOT NULL,
    agreed_price_amount   NUMERIC(10, 2) NOT NULL,
    agreed_price_currency VARCHAR(3)     NOT NULL,
    delivery_days         INT            NOT NULL,
    deadline              DATE           NOT NULL,
    status                VARCHAR(20)    NOT NULL,
    activated_at          TIMESTAMPTZ,
    completed_at          TIMESTAMPTZ,
    version               BIGINT         NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ    NOT NULL,
    updated_at            TIMESTAMPTZ    NOT NULL
);
CREATE INDEX idx_engagements_client ON engagements.engagements (client_id);
CREATE INDEX idx_engagements_student ON engagements.engagements (student_id);
CREATE INDEX idx_engagements_status ON engagements.engagements (status);

CREATE TABLE engagements.deliverables (
    id            UUID PRIMARY KEY,
    engagement_id UUID        NOT NULL REFERENCES engagements.engagements (id) ON DELETE CASCADE,
    file_url      VARCHAR(500),
    link_url      VARCHAR(500),
    note          VARCHAR(600),
    status        VARCHAR(20) NOT NULL,
    submitted_at  TIMESTAMPTZ NOT NULL,
    reviewed_at   TIMESTAMPTZ
);
