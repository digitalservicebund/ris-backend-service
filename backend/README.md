# RIS Backend Service

[![Pipeline](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/pipeline.yml/badge.svg)](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/pipeline.yml)
[![Scan](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/scan.yml/badge.svg)](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/scan.yml)
[![Secrets Check](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/secrets-check.yml/badge.svg)](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/secrets-check.yml)

Java service built with the [Spring WebFlux reactive stack](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#spring-webflux).

## Development

### Setup Secrets

The application depends on a Java package from a private GitHub package repository. To be able to download it in the Gradle build process, you'll need to set up your shell env.

```bash
../run.sh env
```

### Database Setup & Migration with Flyway

The application uses Flyway for maintaining and versioning database migrations. In order to create a change in the database, you should create a new sql file on the directory `src\main\resources\db\migration`.

The file should be named in the following format: `Vx.x__teamname_create_table_xyz.sql` where `x.x` is your migration version (make sure to pull first from the repository and see what is the latest version otherwise migrations wouldn't work properly).
The `teamname` can be replaced with: whether `caselaw` or `norms` and is normally followed by a descriptive name for the migration.

Flyway automatically detects new files and run migrations accordingly on sprint boot start.

### Run Service

Requires the Postgres database to be running: `docker compose up db`

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

**If you use IntelliJ:**

The run configuration _Application_ should be created automatically. Add `local` to _Active profiles_.

**If you use VS Code:**

TODO

### Lookup tables

Some dropdown menus in the frontend get populated via calls to the backend that query the respective database tables. If you are developing locally and want to see values in those dropdown menus you need to do this one-time step: trigger the import of XML files to these database tables. Furthermore, some e2e tests are testing this behaviour. Those will fail locally if your lookup tables are not populated.

These are the endpoints and the respective XML files (find those in our wiki) that need to be uploaded to them:

| Endpoint                                                          | XML file                 |
| ----------------------------------------------------------------- | ------------------------ |
| `http://127.0.0.1/api/v1/caselaw/lookuptableimporter/doktyp`      | `doktyp.xml`             |
| `http://127.0.0.1/api/v1/caselaw/lookuptableimporter/gerichtdata` | `gerichtdata_gesamt.xml` |
| `http://127.0.0.1/api/v1/caselaw/lookuptableimporter/buland`      | `buland.xml`             |

In all cases you need to do a `PUT` call: in _Postman_ go to _Body_, set it to _raw_, change from _Text_ to _XML_ on the blue dropdown to the right and paste the entire XML content in.

## Tests

The project has distinct unit and integration test sets.

**To run just the unit tests:**

```bash
./gradlew test
```

**To run the integration tests:**

```bash
./gradlew integrationTest
```

**Note:** Running integration tests requires passing unit tests (in Gradle terms: integration tests depend on unit
tests), so unit tests are going to be run first. In case there are failing unit tests we won't attempt to continue
running any integration tests.

**To run integration tests exclusively, without the unit test dependency:**

```bash
./gradlew integrationTest --exclude-task test
```

Denoting an integration test is accomplished by using a JUnit 5 tag annotation: `@Tag("integration")`.

Furthermore, there is another type of test worth mentioning. We're
using [ArchUnit](https://www.archunit.org/getting-started)
for ensuring certain architectural characteristics, for instance making sure that there are no cyclic dependencies.

## Formatting & Styleguide

Check our [Java Styleguides](JAVA_STYLEGUIDES.md) document. To set up IntelliJ IDEA follow [these instructions](https://github.com/google/google-java-format#intellij-android-studio-and-other-jetbrains-ides). Consistent formatting, for Java as well as various other types of source code, is being enforced via [Spotless](https://github.com/diffplug/spotless).

**Check formatting:**

```bash
./gradlew spotlessCheck
```

**Autoformat sources:**

```bash
./gradlew spotlessApply
```

## Code quality analysis

Continuous code quality analysis is performed in the pipeline upon pushing to trunk; it requires a
token provided as `SONAR_TOKEN` repository secret that needs to be obtained from https://sonarcloud.io.

**To run the analysis locally:**

```bash
SONAR_TOKEN=[sonar-token] ./gradlew sonarqube
```

Go to [https://sonarcloud.io](https://sonarcloud.io/dashboard?id=digitalservicebund_ris-backend-service) for the analysis results.

## Container image

Container images running the application are automatically published by the pipeline to
the [GitHub Packages Container registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry).

**To run the latest published image:**

```bash
docker run -p8080:8080 "ghcr.io/digitalservicebund/ris-backend-service:$(git log -1 origin/main --format='%H')"
```

The service will be accessible at `http://localhost:8080`.

We are using Spring's built-in support for producing an optimized container image:

```bash
./gradlew bootBuildImage
docker run -p8080:8080 ghcr.io/digitalservicebund/ris-backend-service
```

Container images in the registry are [signed with keyless signatures](https://github.com/sigstore/cosign/blob/main/KEYLESS.md).

**To verify an image**:

```bash
COSIGN_EXPERIMENTAL=1 cosign verify "ghcr.io/digitalservicebund/ris-backend-service:$(git log -1 origin/main --format='%H')"
```

If you need to push a new container image to the registry manually there are two ways to do this:

**Via built-in Gradle task:**

```bash
export CONTAINER_REGISTRY=ghcr.io
export CONTAINER_IMAGE_NAME=digitalservicebund/ris-backend-service
export CONTAINER_IMAGE_VERSION="$(git log -1 --format='%H')"
CONTAINER_REGISTRY_USER=[github-user] CONTAINER_REGISTRY_PASSWORD=[github-token] ./gradlew bootBuildImage --publishImage
```

**Note:** Make sure you're using a GitHub token with the necessary `write:packages` scope for this to work.

**Using Docker:**

```bash
echo [github-token] | docker login ghcr.io -u [github-user] --password-stdin
docker push "ghcr.io/digitalservicebund/ris-backend-service:$(git log -1 --format='%H')"
```

**Note:** Make sure you're using a GitHub token with the necessary `write:packages` scope for this to work.

## Deployment

Changes in trunk are continuously deployed in the pipeline. After the staging deployment, the pipeline runs a verification step
in form of journey tests against staging, to ensure we can safely proceed with deploying to production.

Denoting a journey test is accomplished by using a JUnit 5 tag annotation: `@Tag("journey")`. Journey tests are excluded
from unit and integration test sets.

**To run the journey tests:**

```bash
STAGING_URL=[staging-url] ./gradlew journeyTest
```

When omitting the `STAGING_URL` env variable journey tests run against the local spring application.

## Vulnerability Scanning

Scanning container images for vulnerabilities is performed with [Trivy](https://github.com/aquasecurity/trivy)
as part of the pipeline's `build` job, as well as each night for the latest published image in the container
repository.

To run a scan locally:

```bash
./gradlew bootBuildImage
trivy image --severity HIGH,CRITICAL ghcr.io/digitalservicebund/ris-backend-service:latest
```

## License Scanning

License scanning is performed as part of the pipeline's `build` job. Whenever a production dependency
is being added with a yet unknown license the build is going to fail.

**To run a scan locally:**

```bash
./gradlew checkLicense
```

## Dependency Updates

Can be used to show the latest release version for every dependency. Generate a report in `build/dependencyUpdates/report.txt`.

**To run a scan locally:**

```bash
./gradlew dependencyUpdates -Drevision=release
```
