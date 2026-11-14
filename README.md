# Chambita · Web Services

RESTful API of **Chambita**, the marketplace by Chambita Labs that connects verified university students
with entrepreneurs and certifies their experience on Ethereum Sepolia
(UPC · 1ASI0728 Arquitecturas de Software Emergentes).

## Architecture

Modular monolith with **Spring Boot 4 (Java 21)** and **Spring Modulith**: one module per bounded context,
each with `domain`, `application`, `infrastructure` and `interfaces` layers.

| Module | Responsibility |
|--------|----------------|
| `iam` | Sign-up, sign-in (JWT), institutional email verification |
| `profiles` | Student profile, portfolio and client profile |
| `projects` | Projects and proposals |
| `engagements` | Engagements and deliverables |
| `payments` | Protected (simulated) payment |
| `reputation` | Mutual reviews and reputation |
| `certification` | Experience certificates on Ethereum Sepolia (Web3j + Alchemy) |
| `shared` | Shared Kernel: `Money`, aggregate base class, errors, i18n, OpenAPI, file storage |
| `jobs` | Daily tasks invoked by Vercel Cron |

Contexts communicate through **domain events** stored in the Spring Modulith **Event Publication Registry**
(PostgreSQL), so no event is lost if the container stops. Each context owns a PostgreSQL schema created by Flyway.

## Main endpoints

| Area | Endpoints |
|------|-----------|
| Authentication | `POST /api/v1/authentication/sign-up`, `POST /api/v1/authentication/sign-in`, `GET /api/v1/users/me` |
| Student verification | `POST /api/v1/student-verifications`, `POST /api/v1/student-verifications/confirmations` |
| Profiles | `GET /api/v1/students/{id}`, `PUT /api/v1/students/{id}/profile`, `POST/DELETE /api/v1/students/{id}/portfolio-items`, `GET/PUT /api/v1/clients/{id}` |
| Projects | `POST/GET /api/v1/projects`, `GET /api/v1/projects/{id}`, `GET /api/v1/clients/{id}/projects` |
| Proposals | `POST/GET /api/v1/projects/{id}/proposals`, `POST .../proposals/{proposalId}/acceptance`, `POST .../decline`, `GET /api/v1/students/{id}/proposals` |
| Engagements | `GET /api/v1/engagements`, `GET /api/v1/engagements/{id}`, `POST .../deliverables`, `POST .../approval`, `POST .../cancellation` |
| Payments | `POST/GET /api/v1/engagements/{id}/payments`, `GET /api/v1/students/{id}/payments` |
| Reviews | `POST /api/v1/engagements/{id}/reviews`, `GET /api/v1/users/{id}/reviews`, `GET /api/v1/users/{id}/reputation` |
| Certificates | `GET /api/v1/students/{id}/certificates`, `GET /api/v1/certificates/{id}`, `POST .../revocation`, `GET /api/v1/public/certificates/{hash}` |
| Files | `POST /api/v1/files` (multipart, Supabase Storage) |

Interactive documentation: `/swagger-ui.html` (OpenAPI at `/v3/api-docs`). Messages are returned in en_US by default
and in es_419 with `Accept-Language: es-419`.

## Run locally

Requirements: JDK 21 or newer and PostgreSQL. Set the variables listed in `.env.example`, then:

```bash
./mvnw spring-boot:run
```

To try the API without installing PostgreSQL, run it with an embedded database and test secrets:

```bash
./mvnw spring-boot:test-run
```

Without SMTP, verification codes are written to the log. Without the blockchain variables, certificates stay `PENDING`
until they are configured; the daily job retries them.

## Tests

```bash
./mvnw verify
```

- **Unit tests** (JUnit 5): aggregates `Project`, `Engagement`, `Payment`, `ReviewableEngagement`, `Certificate`, `StudentVerification` and `Money`.
- **Acceptance tests** (Cucumber, Gherkin in `src/test/resources/features`): one feature per technical story
  (TS01–TS06, TS08, TS09, TS11), run against the real API and an embedded PostgreSQL (no Docker needed).
  The mail and blockchain gateways are mocked. Report: `target/cucumber-report.html`.

## Deployment

Vercel **Container** runtime builds the root `Dockerfile` and runs the API on `$PORT`.
`vercel.json` schedules the daily job (`/internal/jobs/daily`, protected with `CRON_SECRET`).
Data lives in Supabase (PostgreSQL through the session pooler and Storage, region sa-east-1).

## Conventions

GitFlow (`main`, `develop`, `feature/*`, `release/*`, `hotfix/*`), Semantic Versioning and Conventional Commits.
Code, identifiers and Gherkin in English; user-facing messages in en_US and es_419.
