# Caipora

Caipora is a web application for managing software, versions, deployments, users, departments, and stakeholders. It is licensed under the [GNU GPL v3](LICENSE.txt).

## Development

Install Java 26 and Docker with Compose. Copy `.env.example` to `.env` and adjust the database credentials. Start PostgreSQL and export the same variables for the application (Compose does not export `.env` to your shell):

```sh
cp .env.example .env
# Edit .env as needed
set -a; . ./.env; set +a
docker compose up -d
```

Compile, test, and package using the Maven wrapper:

```sh
bash mvnw compile
bash mvnw test
bash mvnw clean package
```

Run the application with `bash mvnw spring-boot:run` or `java -jar target/Caipora-0.0.1-SNAPSHOT.jar`, then open <http://localhost:8080>. Flyway initializes a fresh PostgreSQL database; existing SQL Server data requires a separate migration. The initial data includes sample records and an `admin` account with password `12345`; change these credentials before exposing the application. Mailpit is available at <http://localhost:8025>. Stop the services with `docker compose down`.
