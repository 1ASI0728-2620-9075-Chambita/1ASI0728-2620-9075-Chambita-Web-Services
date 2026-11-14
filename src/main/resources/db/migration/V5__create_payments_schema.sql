CREATE TABLE payments.payments (
    id                   UUID PRIMARY KEY,
    engagement_id        UUID           NOT NULL UNIQUE,
    client_id            UUID           NOT NULL,
    student_id           UUID           NOT NULL,
    amount               NUMERIC(10, 2) NOT NULL,
    currency             VARCHAR(3)     NOT NULL,
    service_fee_amount   NUMERIC(10, 2) NOT NULL,
    service_fee_currency VARCHAR(3)     NOT NULL,
    status               VARCHAR(20)    NOT NULL,
    held_at              TIMESTAMPTZ,
    released_at          TIMESTAMPTZ,
    version              BIGINT         NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ    NOT NULL,
    updated_at           TIMESTAMPTZ    NOT NULL
);
CREATE INDEX idx_payments_student ON payments.payments (student_id);
