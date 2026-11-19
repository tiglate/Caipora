# Caipora

Caipora is a web application for managing software, versions, deployments, users, departments, and stakeholders. It is licensed under the [GNU GPL v3](LICENSE.txt).

## Development

Install Java 26 and Docker with Compose. Copy `.env.example` to `.env` and fill it in, then start PostgreSQL:

```sh
cp .env.example .env
# Edit .env: database credentials, ADMIN_INITIAL_PASSWORD, REMEMBER_ME_KEY...
docker compose up -d
```

The application reads `.env` from the working directory automatically when the file exists (real environment
variables take precedence), so no extra step is needed to run it locally. In a container, simply define the
variables in the environment; the file is optional.

Compile, test, package and run using the Maven wrapper:

```sh
./mvnw compile
./mvnw test
./mvnw clean package
./mvnw spring-boot:run      # or: java -jar target/Caipora-0.0.1-SNAPSHOT.jar
```

Then open <http://localhost:8080>. Flyway creates the schema and the reference data (roles).

**Administrator.** When no administrator exists (e.g. a fresh database), the `admin` account is created at startup
with the password from `ADMIN_INITIAL_PASSWORD`; the application refuses to start without it in that situation. That
password is temporary and must be changed at the first sign-in. This happens in every environment.

**Sample data (development only).** With a development profile (`local`, activated by `./mvnw spring-boot:run`, or any
profile containing `dev`), the loaders in the `dev` package fill empty tables with random data generated with
[Datafaker](https://www.datafaker.net/): departments, stakeholders, applications, versions (with placeholder packages
in the upload directory), users and deploys. Nothing is generated without a profile or with `prod`/`uat`/`qa`
profiles. The sample users have random passwords: reset one as administrator to sign in as that user. To start over,
recreate the database (`docker compose down -v && docker compose up -d`).

Mailpit is available at <http://localhost:8025>. Stop the services with `docker compose down`.

### Configuration

| Variable | Purpose | Default |
|---|---|---|
| `REMEMBER_ME_KEY` | Secret (≥ 32 chars) that signs "remember me" cookies. Generate with `openssl rand -base64 48`. | random key per start (remember-me does not survive restarts) |
| `UPLOAD_DIRECTORY` | Where uploaded version packages are stored. | `~/caipora/uploads` |
| `COOKIE_SECURE` | Set to `true` when served over HTTPS. | `false` |
| `ADMIN_INITIAL_PASSWORD` | Initial password of `admin`, used only when no administrator exists; must be changed at the first sign-in. | — (required on the first run) |
| `FORWARD_HEADERS_STRATEGY` | `native`/`framework` behind a reverse proxy, so the real client IP is used. | `none` |

Sign-in is throttled: after 5 failed attempts for a username from the same address, that address is blocked for
15 minutes (`security.login.*` properties). The counters are stored in the database, so they are shared by all
instances. Users created by an administrator, or whose password was reset by one, must choose a new password at
their next sign-in; anyone can change their own password from the user menu. Stop the services with `docker compose down`.
