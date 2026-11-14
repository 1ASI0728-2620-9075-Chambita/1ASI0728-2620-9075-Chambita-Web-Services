CREATE TABLE certification.certificate_holders (
    id             UUID PRIMARY KEY,
    student_id     UUID        NOT NULL UNIQUE,
    holder_id      UUID        NOT NULL UNIQUE,
    holder_address VARCHAR(42) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL
);

CREATE TABLE certification.certificates (
    id               UUID PRIMARY KEY,
    engagement_id    UUID         NOT NULL UNIQUE,
    student_id       UUID         NOT NULL,
    holder_id        UUID         NOT NULL,
    holder_address   VARCHAR(42)  NOT NULL,
    project_title    VARCHAR(120) NOT NULL,
    category         VARCHAR(30)  NOT NULL,
    score            INT          NOT NULL,
    issued_at        TIMESTAMPTZ  NOT NULL,
    certificate_hash VARCHAR(66)  NOT NULL UNIQUE,
    status           VARCHAR(20)  NOT NULL,
    token_id         NUMERIC(78, 0),
    transaction_hash VARCHAR(66),
    attempts         INT          NOT NULL DEFAULT 0,
    last_error       VARCHAR(500),
    revoked_at       TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_certificates_student ON certification.certificates (student_id);
CREATE INDEX idx_certificates_status ON certification.certificates (status);
