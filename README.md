# RIS Backend Service

[![Pipeline](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/pipeline.yml/badge.svg)](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/pipeline.yml)
[![Scan](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/scan.yml/badge.svg)](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/scan.yml)
[![Secrets Check](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/secrets-check.yml/badge.svg)](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/secrets-check.yml)

## Prerequisites

For the provided Git hooks you will need:

```bash
brew install lefthook talisman direnv
```

Additionally install the prerequisites of the [backend](./backend/README.md#Prerequisites) and [frontend](./frontend/README.md#Prerequisites)

## Getting started

**To get started with development run:**

```bash
./run.sh init
```

This will replace placeholders in the application template and install a couple of Git hooks.

**Setup local env**

Add this to the end of .zshrc (see [here](https://github.com/direnv/direnv/blob/master/docs/hook.md)):

```bash
eval "$(direnv hook zsh)"
```

Allow direnv to use `.env` files (see [here](https://github.com/direnv/direnv/blob/master/man/direnv.toml.1.md#codeloaddotenvcode))

```bash
 cat > ~/.config/direnv/direnv.toml<< EOF
[global]
load_dotenv = true
EOF
```

Create .env file (repeat whenever values in gopass change)

```bash
./run.sh env
direnv allow .
```

to test

```bash
echo $OAUTH2_CLIENT_ID
```

To launch from IDE, add the `.env` file in repo root. e.g. in vscode `launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Launch Application",
      "request": "launch",
      "mainClass": "de.bund.digitalservice.ris.Application",
      "projectName": "ris-backend-service",
      "envFile": "${workspaceFolder}/../.env",
      "args": "--spring.profiles.active=local"
    }
  ]
}
```

## Development

```bash
./run.sh dev
```

This will start the backend with a Postgres database and [utilizing Spring Boot developer tools](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.devtools.restart)
so changes in the Java sources will be reflected without manually restarting. Similarly, the frontend is served from [Vite](https://vitejs.dev) with [HMR](https://vitejs.dev/guide/features.html#hot-module-replacement).

Read the component individual documentation to figure out how to run them individually:

- [backend](./backend/README.md#development)
- [frontend](./frontend/README.md#development)

## Git hooks

The repo contains a [Lefthook](https://github.com/evilmartians/lefthook/blob/master/docs/full_guide.md) configuration,
providing a Git hooks setup out of the box.

**To install these hooks, run:**

```bash
./run.sh init
```

The hooks are supposed to help you to:

- commit properly formatted source code only (and not break the build otherwise)
- write [conventional commit messages](https://chris.beams.io/posts/git-commit/)
- not accidentally push [secrets and sensitive information](https://thoughtworks.github.io/talisman/)

## Architecture Decision Records

[Architecture decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)
are kept in the `docs/adr` directory. For adding new records install the [adr-tools](https://github.com/npryce/adr-tools) package:

```bash
brew install adr-tools
```

See https://github.com/npryce/adr-tools regarding usage.

## Slack notifications

Opt in to CI posting notifications for failing jobs to a particular Slack channel by setting a repository secret
with the name `SLACK_WEBHOOK_URL`, containing a url for [Incoming Webhooks](https://api.slack.com/messaging/webhooks).
