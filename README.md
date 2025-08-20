# RIS Caselaw

[![Pipeline](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/pipeline.yml/badge.svg)](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/pipeline.yml)
[![Scan](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/scan.yml/badge.svg)](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/scan.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=digitalservicebund_ris-backend-service_backend&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=digitalservicebund_ris-backend-service_backend)
[![Renovate enabled](https://img.shields.io/badge/renovate-enabled-brightgreen.svg)](https://renovatebot.com/)

## Prerequisites

You need (or may want) the following CLI tools. For UNIX users, there is a prepared `Brewfile`, see
below.

### Necessary tools:**

- [lefthook](https://github.com/evilmartians/lefthook#install) - manages our git hooks
- [Github CLI](https://cli.github.com/) - used by lefthook to check for pipeline status before push
- [docker](https://docs.docker.com/get-docker/) - our container runtime (on macOS, the easiest way
  is to use [Docker Desktop](https://www.docker.com/products/docker-desktop/))
- [1Password CLI](https://developer.1password.com/docs/cli/get-started/) - to access secrets
- [Node.js](https://nodejs.org/en/) - JavaScript runtime & dependency management
- [nodenv](https://github.com/nodenv/nodenv#installation) - manages the node.js environment

### Backend only:**

- [java](https://developers.redhat.com/products/openjdk/install) - we use Java 21 in the backend

### Optional, but recommended tools:

- [jq](https://github.com/stedolan/jq) - handy JSON Processor
- [yq](https://github.com/mikefarah/yq) - handy YAML Processor
- [actionlint](https://github.com/rhysd/actionlint/blob/main/docs/install.md) - a tool that lints
  GitHub Action pipeline
  definitions
- [shellcheck](https://github.com/koalaman/shellcheck#installing) - shell script analyzer, that also
  provides extensions
  for [VS Code](https://marketplace.visualstudio.com/items?itemName=timonwong.shellcheck)
- [trivy](https://github.com/aquasecurity/trivy#get-trivy) - our vulnerability scanner
- [adr-tools](https://github.com/npryce/adr-tools) - a command-line tool to manage
  our [Architecture Decision Records (ADRs)](#architecture-decision-records)
- [direnv](https://github.com/direnv/direnv/blob/master/docs/installation.md) - manages our local
  environment

If you use [`homebrew`](https://brew.sh/), you can simply execute this to to install all required
and optional
dependencies:

```bash
brew bundle
```

If you decided to install `direnv`, you have to hook it onto your shell as
described [here](https://github.com/direnv/direnv/blob/master/docs/hook.md). E.g. for ZSH add this
to `~/.zshrc`:

```bash
eval "$(direnv hook zsh)"
```

### S3 Credentials for Lookup Table Initialization

The lookup table initialization in your local environment will be performed with data provided by a s3 bucket. Read [here](https://platform-docs.prod.ds4g.net/user-docs/how-to-guides/access-obs-via-aws-sdk/#step-2-obtain-access_key-credentials) on how to retrieve credentials for it.

Then, store the credentials in 1Password:

```shell
op item create --category login --title 'NeuRIS S3' \
'access-key-id=[your-access-key-id]' \
'secret-access-key=[your-secret-access-key]'
```

## Getting started

To get started with development, run:

```bash
./run.sh init
```

This will install a couple of Git hooks which are supposed to help you to:

- commit properly formatted source code only (and not break the build otherwise)
- write [conventional commit messages](https://chris.beams.io/posts/git-commit/)
- not accidentally push on a failing pipeline

Also, it creates a new `.env` file containing the secrets. You will be asked to authorize requests to 1Password.

> **Note**
>
> This needs to be repeated every time the secrets change.

### Lookup Tables Initialization

The caselaw application requires the initialization of lookup tables by the migration application image.

The following command will migrate the minimally required data (refdata and juris tables):

Make sure the latest
[ris data migration image](https://github.com/digitalservicebund/ris-data-migration/pkgs/container/ris-data-migration)
is
in [compose.yaml](https://github.com/digitalservicebund/ris-backend-service/blob/40aec11b48cfb839a2103db2932ca6b74ed15448/compose.yaml#L3-L3)
and then run:

```bash
./run.sh dev -i
```

> Note: If you wish to migrate documentation units, use the instructions
> in [run_migration_locally.md](run_migration_locally.md)

## Development

Run the whole stack including migration (initialization) inside docker:

```bash
./run.sh dev
```

If you don't want to watch the log stream but let Docker perform health checks until everything is
up, use detached
mode:

```bash
./run.sh dev -d
./run.sh dev --detached
```

To run the frontend stack only (without backend and initialization) run:

```bash
./run.sh dev -n
./run.sh dev --no-backend
```

When choosing the no-backend variant, checkout the [backend manual](./backend/README.md) on how to run the backend
stand-alone without docker. The easiest way would be to start the
backend [utilizing Spring Boot developer tools](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.devtools.restart)
so changes in the Java sources will be reflected without manually restarting:

```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

> Note: Similarly, the frontend is served from [Vite](https://vitejs.dev)
> with [HMR](https://vitejs.dev/guide/features.html#hot-module-replacement)

Overall docker compose spins up a reverse proxy (traefik) which listens on port 80. Therefore the application is
available at <http://127.0.0.1>. If you get a `bad gateway` error make sure your firewall is not messing with you. On
Ubuntu `sudo ufw disable` might do the trick. You may setup a certain firewall rule. Overall your milage may vary.

> **Note**
>
> When first starting the development server, dependencies will be installed automatically. This
> includes supported
> browsers for E2E and a11y testing through playwright. Should that fail, you
> can [install them manually](https://github.com/digitalservicebund/ris-backend-service/tree/main/frontend#prerequisites).

To see logs of the containers, use e.g.

```bash
docker compose logs # for all
docker compose logs frontend # for specific services
```

To stop the whole environment:

```bash
./run.sh down
```

Read the component individual documentation to figure out how to run them individually:

- [backend](./backend/README.md#development)
- [frontend](./frontend/README.md#development)

## Deployment

The pipeline performs the deployment through GitOps using [ArgoCD](https://argoproj.github.io/cd/) (
see [example pipeline deploy step definition](https://github.com/digitalservicebund/ris-backend-service/blob/main/.github/workflows/pipeline.yml#L657-L667)):

- Build and push the new Docker image (see here)
- Commit the new tag in the deployment manifest in the neuris-infra repository
- Sync the respective ArgoCD App, which will cause ArgoCD to apply all changed Kubernetes manifests
  on the cluster to
  create the desired state

## Tests

You can run both frontend and backend tests simultaneously with the following commit:

```bash
lefthook run tests
```

## API Documentation

To access the api documentation, start the application and navigate to `/api/docs.html`
or `/api/docs.json` in your
browser.

## Architecture Decision Records

[Architecture decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
are kept in
the [doc/adr](doc/adr) directory and are managed
with [adr-tools](https://github.com/npryce/adr-tools).

## Slack notifications

Opt in to CI posting notifications for failing jobs to a particular Slack channel by setting a
repository secret
with the name `SLACK_WEBHOOK_URL`, containing a url
for [Incoming Webhooks](https://api.slack.com/messaging/webhooks).

## Reports

All reports will be published here https://digitalservicebund.github.io/ris-reports/
