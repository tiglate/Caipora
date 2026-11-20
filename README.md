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
in the packages directory), users and deploys. Nothing is generated without a profile or with `prod`/`uat`/`qa`
profiles. The sample users have random passwords: reset one as administrator to sign in as that user. To start over,
recreate the database (`docker compose down -v && docker compose up -d`).

Mailpit is available at <http://localhost:8025>. Stop the services with `docker compose down`.

### Configuration

| Variable | Purpose | Default |
|---|---|---|
| `REMEMBER_ME_KEY` | Secret (≥ 32 chars) that signs "remember me" cookies. Generate with `openssl rand -base64 48`. | random key per start (remember-me does not survive restarts) |
| `PACKAGES_DIRECTORY` | Directory with one folder per application, named after its code, holding the version packages. | `~/caipora/packages` |
| `PACKAGE_EXTENSIONS` | Accepted package extensions. | `zip,msi,exe,jar,war,ear,tar.gz,tgz,7z,nupkg` |
| `DEPLOY_INTERPRETER` | Command that runs deploy scripts (comma-separated arguments; the script file is appended). | `bash` |
| `DEPLOY_SCRIPT_EXTENSION` | Extension of the temporary script file (`.sh`, `.ps1`, `.cmd`...). | `.sh` |
| `DEPLOY_WORKING_DIRECTORY` | Working directory of deploy scripts. | the system temporary directory |
| `DEPLOY_TIMEOUT` | Maximum duration of a deploy script (ISO-8601). | `PT30M` |
| `CAIPORA_NODE_ID` | Name of this instance, recorded with the deploys it runs. | host name |
| `COOKIE_SECURE` | Set to `true` when served over HTTPS. | `false` |
| `ADMIN_INITIAL_PASSWORD` | Initial password of `admin`, used only when no administrator exists; must be changed at the first sign-in. | — (required on the first run) |
| `FORWARD_HEADERS_STRATEGY` | `native`/`framework` behind a reverse proxy, so the real client IP is used. | `none` |

### Packages and deploys

Version packages are not uploaded through the browser. Copy them (file share, CI pipeline, `scp`...) to the folder of
the application inside `PACKAGES_DIRECTORY`, e.g. `/srv/caipora/packages/CRM/crm-2.4.1.zip`; the folder is created
when the application is saved, and the application code cannot change while it has versions. Registering a version
means picking one of the files of that folder that is not registered yet. Deleting a version keeps its file.

Each application may have a **deploy script**, run on the Caipora server for every deploy: the deploy succeeds when
the script exits with code 0, and only then becomes the active version of the environment. The script receives the
deploy in environment variables (`CAIPORA_PACKAGE` — full path of the package —, `CAIPORA_PACKAGE_NAME`,
`CAIPORA_PACKAGES_FOLDER`, `CAIPORA_APP_CODE`, `CAIPORA_APP_NAME`, `CAIPORA_VERSION`, `CAIPORA_ENVIRONMENT`,
`CAIPORA_RFC`, `CAIPORA_DEPLOY_ID`, `CAIPORA_OPERATOR`); its output is shown live on the deploy page. Without a script,
deploys are only recorded. Only one deploy of an application per environment runs at a time; a failed deploy can be
run again with the same RFC, and *Unlock* cancels deploys left stuck. On Windows servers use, for instance,
`DEPLOY_INTERPRETER=powershell.exe,-NoProfile,-NonInteractive,-ExecutionPolicy,Bypass,-File` and
`DEPLOY_SCRIPT_EXTENSION=.ps1`.

Editing a script means running commands on the server with the permissions of the Caipora process, so it requires
its own permission, *Deploy Script* (`ROLE_DEPLOY_SCRIPT_MANAGE`), granted by the V003 migration to existing
administrators. Run Caipora with an account limited to what deploys need. With several instances, the packages
directory must be shared storage, and each instance runs the deploys it starts.

Versions registered before packages moved to `PACKAGES_DIRECTORY` point to files of the former upload directory
(named `<uuid>_<name>`): move them to the folder of their application, without the prefix, and update `file_name`
accordingly, or they are shown as missing and cannot be deployed with a script.

Sign-in is throttled: after 5 failed attempts for a username from the same address, that address is blocked for
15 minutes (`security.login.*` properties). The counters are stored in the database, so they are shared by all
instances. Users created by an administrator, or whose password was reset by one, must choose a new password at
their next sign-in; anyone can change their own password from the user menu. Stop the services with `docker compose down`.
