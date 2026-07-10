# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

MatchWiz backend: a Quarkus / Java REST API for predicting soccer match
results within private groups. Backed by PostgreSQL + Flyway, JWT auth. Pairs
with a separate Angular frontend (not in this repo). See `SPEC.md` for the full
functional/domain spec (roles, competition model, scoring rules) and
`openapi.yaml` for the API contract this backend implements exactly
(operationIds, paths, status codes).

**This is Quarkus, not Spring** — no `@SpringBootApplication`, no Spring Data,
no Spring Security. Entities are Hibernate ORM with **Panache** (active-record
style, static finder methods on the entity itself, e.g. `AppUser.findById(...)`).

## Commands

```shell
./mvnw quarkus:dev              # dev mode, live reload; Dev UI at http://localhost:8080/q/dev/
./mvnw test                     # run all tests
./mvnw test -Dtest=ScoringServiceTest        # run a single test class
./mvnw test -Dtest=ScoringServiceTest#someMethod   # run a single test method
./mvnw package                  # build target/quarkus-app/quarkus-run.jar
./mvnw package -Dnative         # native executable (GraalVM, or add -Dquarkus.native.container-build=true)
```

Dev and test modes provision PostgreSQL automatically via Quarkus Dev Services
(Testcontainers — Docker required); no manual DB setup needed locally.

## Architecture

### Layering

`resource` (JAX-RS endpoints, one class per OpenAPI tag, `@ApplicationPath("/api")`)
→ `service` (business logic) → `entity` (Panache entities, static finders) with
`dto` (records) for the wire format. Entities share a `BaseEntity` with
`IDENTITY` id generation, matching the Flyway-managed schema. DTOs use
`from(entity)` static mapper methods and Bean Validation annotations; Jackson
serializes as camelCase to match the OpenAPI contract schemas.

Flyway (`src/main/resources/db/migration/V*.sql`) owns the schema —
`quarkus.hibernate-orm.schema-management.strategy=none`, so Hibernate never
auto-manages or validates it against the entities.

### Authorization model

Two independent layers, because the OpenAPI contract only specifies
`bearerAuth` and doesn't encode group-level roles in the token:

- **Global role** (`USER`/`ADMIN`) lives in the JWT and is checked declaratively
  via `@RolesAllowed("ADMIN")` on resource methods.
- **Group role** (`MEMBER`/`GROUP_ADMIN`) is *not* in the JWT — it's looked up
  per-request from `GroupMembership` and enforced programmatically via
  `security/GroupAuthz.requireMember(groupId)` /
  `requireGroupAdmin(groupId)`. Global `ADMIN` bypasses both checks.
  `security/CurrentUser` resolves the authenticated `AppUser` from the JWT
  subject (request-scoped, cached per request).
- The first approved member of a group is auto-promoted to `GROUP_ADMIN`
  (the contract has no explicit promote endpoint).
- Registration approval: new users are created inactive but can already log
  in; `approveMember` activates the account and approves the membership in
  one step.

### Config

All runtime config is env-var driven with local dev defaults baked into
`application.properties` (`MATCHWIZ_DB_*`, `MATCHWIZ_ADMIN_USERNAME/PASSWORD`,
`MATCHWIZ_CORS_ORIGINS`, `MATCHWIZ_JWT_*_KEY_LOCATION`, `MATCHWIZ_SMTP_*`,
`MATCHWIZ_NOTIFY_EMAIL`, `MATCHWIZ_REMINDER_CRON`) — see the table in
`README.md` for the full list. `publicKey.pem`/`privateKey.pem` under
`src/main/resources` are committed **dev-only** JWT keys; production must
point `MATCHWIZ_JWT_*_KEY_LOCATION` at a mounted secret instead.

### Email / verification tokens

`EmailService` sends mail via Quarkus Mailer (mocked/logged in dev & test —
`quarkus.mailer.mock`). Password reset and email verification both go through
a shared `entity/VerificationToken` (+ `VerificationTokenType`) table rather
than separate mechanisms, so a new token type (e.g. login OTP) can reuse it.
Password resets are plain codes returned/emailed to the user — there is no
frontend reset link to build a URL for.

### Scoring & matchdays

`ScoringService` rescoring is triggered when a `Match` transitions to
`FINISHED`. `MatchdayReminderService` runs on a daily cron
(`matchwiz.reminder.cron`, default 08:00 UTC) to email upcoming-matchday
reminders, converted to each user's own timezone; `ReminderLog` prevents
duplicate sends. `RankingService` orders group standings by total points ↓,
then exact-score count ↓, then username ↑.

### Bootstrap / seeders (`bootstrap` package)

`AdminSeeder` idempotently seeds one config-driven global `ADMIN`
(`matchwiz.admin.username/password`) on every startup. `TestDataSeeder` and
`WorldCup2026Seeder` are dev-only, off by default except `test-data` in dev
(`matchwiz.seeders.*.enabled`). `StartupNotifier` optionally emails
`MATCHWIZ_NOTIFY_EMAIL` when the app starts.

### Tests

`MatchWizSmokeTest` is the main end-to-end spine (Testcontainers Postgres +
REST Assured): login → competition → teams/matchday/match → register/invite/
approve → predict → result → ranking, plus deadline-rejection, unauth-401, and
a non-global-admin `GROUP_ADMIN` assertion. `EmailVerificationFlowTest` /
`PasswordResetFlowTest` cover those flows end-to-end. `ScoringServiceTest` is
a pure unit test of all scoring outcomes — prefer it as the template for new
service-level unit tests over spinning up another full E2E test.
