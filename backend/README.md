# RIS Backend Service

[![Pipeline](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/pipeline.yml/badge.svg)](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/pipeline.yml)
[![Scan](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/scan.yml/badge.svg)](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/scan.yml)
[![Secrets Check](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/secrets-check.yml/badge.svg)](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/secrets-check.yml)

Java service built with the [Spring WebFlux reactive stack](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#spring-webflux).

## Development

### Run Service

Requires the all but backend to be running in docker:

```bash
../run.sh dev --no-backend
```

**Start backend with IntelliJ:**

- Use the checked in run config in `../.idea/runConfigurations/`. If you open just this `backend` folder with IntelliJ, you will need to copy and adjust it.

**Start backend with VS Code:**

- The launch config in `.vscode/launch.json` should be used automatically

**Start backend from CLI**:

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

**Note:** The application depends on a Java package from a private GitHub package repository. To be able to download it in the Gradle build process, you'll need to set up your local env as described in the [root readme](../README.md#setup-local-env).

### Lookup tables

Some dropdown menus in the frontend get populated via calls to the backend that query the respective database tables. If you are developing locally and want to see values in those dropdown menus you need to do this one-time step: trigger the import of XML files to these database tables. Furthermore, some e2e tests are testing this behaviour. Those will fail locally if your lookup tables are not populated.

To import the XML files, follow these steps:

- Download the XML files `doktyp.xml`, `gerichtdata_gesamt.xml`, `buland.xml`, `sachneudata_gesamt.xml` (Link in the Engineering Onboarding WIki)
- Start the application (see [root README](../README.md)), open it in your browser and log in
- Copy the `SESSION` cookie value from the Browser Developer Tools --> Application Tab --> Cookies
  (If you prefer using Postman, it also supports [importing cookies](https://github.com/digitalservicebund/ris-backend-service/commit/69684a3872ce9875484761fcb18f3367d0143bce#commitcomment-99597762) from your browser.)

Fill these variables with your values:

```bash
export PATH_TO_XML_FILES="/path/to/xml/files"    # where you placed the xml files
export SESSION_VALUE="your-session-cookie-value" # copied from Browser Developer Tools
```

Then do the requests with curl:

```bash
curl -v -X PUT -H 'Content-Type: application/xml' -H "cookie: SESSION=$SESSION_VALUE" --data "@$PATH_TO_XML_FILES/doktyp.xml" http://127.0.0.1/api/v1/caselaw/lookuptableimporter/doktyp
curl -v -X PUT -H 'Content-Type: application/xml' -H "cookie: SESSION=$SESSION_VALUE" --data "@$PATH_TO_XML_FILES/gerichtdata_gesamt.xml" http://127.0.0.1/api/v1/caselaw/lookuptableimporter/gerichtdata
curl -v -X PUT -H 'Content-Type: application/xml' -H "cookie: SESSION=$SESSION_VALUE" --data "@$PATH_TO_XML_FILES/buland.xml" http://127.0.0.1/api/v1/caselaw/lookuptableimporter/buland
curl -v -X PUT -H 'Content-Type: application/xml' -H "cookie: SESSION=$SESSION_VALUE" --data "@$PATH_TO_XML_FILES/sachneudata_gesamt.xml" http://127.0.0.1/api/v1/caselaw/lookuptableimporter/fieldOfLaw
```

### Database Setup & Migration with Flyway

The application uses Flyway for maintaining and versioning database migrations. In order to create a change in the database, you should create a new sql file on the directory `src\main\resources\db\migration`.

The file should be named in the following format: `Vx.x__teamname_create_table_xyz.sql` where `x.x` is your migration version (make sure to pull first from the repository and see what is the latest version otherwise migrations wouldn't work properly).
The `teamname` can be replaced with: whether `caselaw` or `norms` and is normally followed by a descriptive name for the migration.

Flyway automatically detects new files and run migrations accordingly on sprint boot start.

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

Furthermore, there is another type of test worth mentioning. We're using [ArchUnit](https://www.archunit.org/getting-started) for ensuring certain architectural characteristics, for instance making sure that there are no cyclic dependencies.

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
