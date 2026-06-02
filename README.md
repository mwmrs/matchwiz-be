# matchwiz-be

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Configuration & secrets

The backend is configured via environment variables (sensible local defaults are
provided in `src/main/resources/application.properties`):

| Variable | Purpose | Default |
| --- | --- | --- |
| `MATCHWIZ_DB_URL` / `MATCHWIZ_DB_USER` / `MATCHWIZ_DB_PASSWORD` | PostgreSQL connection (prod profile) | local `matchwiz` db |
| `MATCHWIZ_ADMIN_USERNAME` / `MATCHWIZ_ADMIN_PASSWORD` | Bootstrap ADMIN seeded on startup | `admin` / `admin` |
| `MATCHWIZ_CORS_ORIGINS` | Allowed CORS origins | `http://localhost:4200` |
| `MATCHWIZ_JWT_PRIVATE_KEY_LOCATION` | JWT signing (private) key location | classpath `privateKey.pem` |
| `MATCHWIZ_JWT_PUBLIC_KEY_LOCATION` | JWT verification (public) key location | classpath `publicKey.pem` |

In dev/test mode the database is provisioned automatically by Quarkus Dev Services
(Testcontainers PostgreSQL — Docker required).

### JWT signing keys

`src/main/resources/publicKey.pem` and `privateKey.pem` are **DEV-ONLY** keys,
committed so that `git clone` + dev mode + tests work out of the box. **They must
not be used in production.**

For production, generate a dedicated keypair, store the private key as a secret
(e.g. a mounted file / Docker secret), and point the app at it:

```shell
openssl genrsa -out priv.pem 2048
openssl pkcs8 -topk8 -nocrypt -in priv.pem -out jwt-private.pem
openssl rsa -in priv.pem -pubout -out jwt-public.pem

# then run with absolute paths to the mounted secret files:
export MATCHWIZ_JWT_PRIVATE_KEY_LOCATION=/run/secrets/jwt-private.pem
export MATCHWIZ_JWT_PUBLIC_KEY_LOCATION=/run/secrets/jwt-public.pem
```

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

---

What was built

  A complete Quarkus 3.36 / Java 21 backend satisfying every operation in openapi.yaml, backed by PostgreSQL with JWT auth:

  - Schema — Flyway V1__init.sql (11 tables, FKs, unique constraints, indexes). Entities use a shared BaseEntity with IDENTITY id
  generation to match the migration.
  - Entities / DTOs — Panache entities + record DTOs with from(entity) mappers and Bean Validation; camelCase JSON via Jackson,
  matching the contract schemas.
  - Security — bcrypt passwords, RSA-signed JWTs (global role in the groups claim). Authorization enforced by us since the contract
  only says bearerAuth: @RolesAllowed("ADMIN") for global-admin ops; GroupAuthz checks per-group GROUP_ADMIN/membership
  programmatically (never in the token).
  - Services — auth, competition (+ default 5/3/2 scoring rule), group/membership, invitation (stub/log email), team, matchday, match,
  prediction (upsert, deadline-enforced), scoring (FINISHED triggers rescore), ranking (total↓ → exact↓ → username↑), notifications.
  - Resources — one per tag, exact operationIds/paths/status codes; concrete root paths (no @Path("/") after the routing fix).
  @ApplicationPath("/api") keeps /q/* at root.
  - Bootstrap — idempotent config-driven ADMIN seed on startup.
  - Tests — pure-unit scoring (all 4 outcomes) + E2E smoke spine against a Testcontainers Postgres (login → competition →
  teams/matchday/match → register/invite/approve → predict → result → ranking), plus deadline-rejection, unauth-401, and a
  non-global-admin GROUP_ADMIN assertion.

  Decisions you made along the way (all implemented)

  - Email: stub/log (invite token returned in response); 2FA: flag only.
  - Bootstrap admin: config-driven idempotent seed (MATCHWIZ_ADMIN_USERNAME/PASSWORD, default admin/admin).
  - Registration approval: users are inactive but can log in; approveMember activates the account + approves membership.
  - GROUP_ADMIN: first approved member of a group is auto-promoted (contract has no promote endpoint).

  Two things to note (non-blocking, for prod)

  - src/main/resources/privateKey.pem is committed — fine for MVP self-host, but move it to a mounted secret/env var for production.
  - schema-management.strategy=none means Hibernate won't validate mappings at boot (Flyway owns the schema; tests prove the
  mappings). Set it to validate if you want column-drift caught at startup.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/matchwiz-be-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
