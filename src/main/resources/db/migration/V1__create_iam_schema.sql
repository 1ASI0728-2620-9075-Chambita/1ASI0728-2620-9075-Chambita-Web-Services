CREATE TABLE iam.users (
    id               UUID PRIMARY KEY,
    email            VARCHAR(160) NOT NULL UNIQUE,
    password_hash    VARCHAR(255) NOT NULL,
    full_name        VARCHAR(120) NOT NULL,
    role             VARCHAR(20)  NOT NULL,
    status           VARCHAR(30)  NOT NULL,
    student_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL
);

CREATE TABLE iam.student_verifications (
    id                  UUID PRIMARY KEY,
    user_id             UUID         NOT NULL REFERENCES iam.users (id),
    institutional_email VARCHAR(160) NOT NULL,
    code_hash           VARCHAR(255) NOT NULL,
    expires_at          TIMESTAMPTZ  NOT NULL,
    attempts            INT          NOT NULL DEFAULT 0,
    status              VARCHAR(20)  NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL
);
CREATE INDEX idx_student_verifications_user ON iam.student_verifications (user_id, status);

CREATE TABLE iam.university_domains (
    id              UUID PRIMARY KEY,
    domain          VARCHAR(80)  NOT NULL UNIQUE,
    university_name VARCHAR(160) NOT NULL
);

INSERT INTO iam.university_domains (id, domain, university_name) VALUES
    (gen_random_uuid(), 'upc.edu.pe', 'Universidad Peruana de Ciencias Aplicadas'),
    (gen_random_uuid(), 'pucp.edu.pe', 'Pontificia Universidad Católica del Perú'),
    (gen_random_uuid(), 'ulima.edu.pe', 'Universidad de Lima'),
    (gen_random_uuid(), 'unmsm.edu.pe', 'Universidad Nacional Mayor de San Marcos'),
    (gen_random_uuid(), 'uni.edu.pe', 'Universidad Nacional de Ingeniería'),
    (gen_random_uuid(), 'up.edu.pe', 'Universidad del Pacífico'),
    (gen_random_uuid(), 'usil.edu.pe', 'Universidad San Ignacio de Loyola'),
    (gen_random_uuid(), 'utp.edu.pe', 'Universidad Tecnológica del Perú'),
    (gen_random_uuid(), 'upn.edu.pe', 'Universidad Privada del Norte'),
    (gen_random_uuid(), 'usmp.pe', 'Universidad de San Martín de Porres'),
    (gen_random_uuid(), 'urp.edu.pe', 'Universidad Ricardo Palma'),
    (gen_random_uuid(), 'ucsur.edu.pe', 'Universidad Científica del Sur'),
    (gen_random_uuid(), 'esan.edu.pe', 'Universidad ESAN'),
    (gen_random_uuid(), 'utec.edu.pe', 'Universidad de Ingeniería y Tecnología'),
    (gen_random_uuid(), 'unfv.edu.pe', 'Universidad Nacional Federico Villarreal'),
    (gen_random_uuid(), 'lamolina.edu.pe', 'Universidad Nacional Agraria La Molina');
