CREATE TABLE projects.projects (
    id              UUID PRIMARY KEY,
    client_id       UUID           NOT NULL,
    title           VARCHAR(120)   NOT NULL,
    description     VARCHAR(2000)  NOT NULL,
    category        VARCHAR(30)    NOT NULL,
    required_skills TEXT[]         NOT NULL DEFAULT '{}',
    deliverables    TEXT[]         NOT NULL DEFAULT '{}',
    budget_amount   NUMERIC(10, 2) NOT NULL,
    budget_currency VARCHAR(3)       NOT NULL,
    deadline        DATE           NOT NULL,
    assisted        BOOLEAN        NOT NULL DEFAULT FALSE,
    status          VARCHAR(20)    NOT NULL,
    version         BIGINT         NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ    NOT NULL,
    updated_at      TIMESTAMPTZ    NOT NULL
);
CREATE INDEX idx_projects_status ON projects.projects (status, created_at DESC);
CREATE INDEX idx_projects_client ON projects.projects (client_id);

CREATE TABLE projects.proposals (
    id             UUID PRIMARY KEY,
    project_id     UUID           NOT NULL REFERENCES projects.projects (id) ON DELETE CASCADE,
    student_id     UUID           NOT NULL,
    price_amount   NUMERIC(10, 2) NOT NULL,
    price_currency VARCHAR(3)       NOT NULL,
    delivery_days  INT            NOT NULL,
    message        VARCHAR(600)   NOT NULL,
    status         VARCHAR(20)    NOT NULL,
    created_at     TIMESTAMPTZ    NOT NULL,
    UNIQUE (project_id, student_id)
);
CREATE INDEX idx_proposals_student ON projects.proposals (student_id);
