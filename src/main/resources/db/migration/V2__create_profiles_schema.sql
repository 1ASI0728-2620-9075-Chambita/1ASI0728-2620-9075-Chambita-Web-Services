CREATE TABLE profiles.student_profiles (
    id                 UUID PRIMARY KEY,
    user_id            UUID          NOT NULL UNIQUE,
    full_name          VARCHAR(120)  NOT NULL,
    university         VARCHAR(160),
    career             VARCHAR(120),
    cycle              INT,
    bio                VARCHAR(600),
    skills             TEXT[]        NOT NULL DEFAULT '{}',
    service_categories TEXT[]        NOT NULL DEFAULT '{}',
    rating_average     NUMERIC(3, 2) NOT NULL DEFAULT 0,
    rating_count       INT           NOT NULL DEFAULT 0,
    certificate_count  INT           NOT NULL DEFAULT 0,
    created_at         TIMESTAMPTZ   NOT NULL,
    updated_at         TIMESTAMPTZ   NOT NULL
);

CREATE TABLE profiles.portfolio_items (
    id                 UUID PRIMARY KEY,
    student_profile_id UUID         NOT NULL REFERENCES profiles.student_profiles (id) ON DELETE CASCADE,
    title              VARCHAR(120) NOT NULL,
    description        VARCHAR(600),
    image_url          VARCHAR(500),
    external_url       VARCHAR(500),
    created_at         TIMESTAMPTZ  NOT NULL
);

CREATE TABLE profiles.client_profiles (
    id             UUID PRIMARY KEY,
    user_id        UUID          NOT NULL UNIQUE,
    display_name   VARCHAR(120)  NOT NULL,
    business_name  VARCHAR(120),
    district       VARCHAR(80),
    rating_average NUMERIC(3, 2) NOT NULL DEFAULT 0,
    rating_count   INT           NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ   NOT NULL,
    updated_at     TIMESTAMPTZ   NOT NULL
);
