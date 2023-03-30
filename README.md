# RIS Backend Service

[![Pipeline](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/pipeline.yml/badge.svg)](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/pipeline.yml)
[![Scan](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/scan.yml/badge.svg)](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/scan.yml)
[![Secrets Check](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/secrets-check.yml/badge.svg)](https://github.com/digitalservicebund/ris-backend-service/actions/workflows/secrets-check.yml)

## Prerequisites

You need (or may want) the following CLI tools. For UNIX users, there is a prepared `Brewfile`, see below.

**Necessary tools:**

- [lefthook](https://github.com/evilmartians/lefthook#install) - manages our git hooks
- [talisman](https://thoughtworks.github.io/talisman/docs) - scans for secrets before you would commit them
- [docker](https://docs.docker.com/get-docker/) - our container runtime
- [gopass](https://www.gopass.pw/#install) - a tool to sync secrets
- [direnv](https://github.com/direnv/direnv/blob/master/docs/installation.md) - manages our local environment
- [node.js](https://nodejs.org/en/) - JavaScript runtime & dependency management
- [nodenv](https://github.com/nodenv/nodenv#installation) - manages the node.js Environment

**Backend only:**

- [java](https://developers.redhat.com/products/openjdk/install) - we use Java 17 in the backend

**Optional, but recommended tools:**

- [jq](https://github.com/stedolan/jq) - handy JSON Processor
- [yq](https://github.com/mikefarah/yq) - handy YAML Processor
- [actionlint](https://github.com/rhysd/actionlint/blob/main/docs/install.md) - a tool that lints GitHub Action pipeline definitions
- [shellcheck](https://github.com/koalaman/shellcheck#installing) - shell script analyzer, that also provides extensions for [VS Code](https://marketplace.visualstudio.com/items?itemName=timonwong.shellcheck)
- [trivy](https://github.com/aquasecurity/trivy#get-trivy) - our vulnerability scanner
- [adr-tools](https://github.com/npryce/adr-tools) - a command-line tool to manage our [Architecture Decision Records (ADRs)](#architecture-decision-records)

If you use [homebrew](https://brew.sh/), you can simply execute this to to install all required and optional dependencies

```bash
brew bundle
```

## Getting started

To get started with development run:

```bash
./run.sh init
```

This will install a couple of Git hooks which are supposed to help you to:

- commit properly formatted source code only (and not break the build otherwise)
- write [conventional commit messages](https://chris.beams.io/posts/git-commit/)
- not accidentally push [secrets and sensitive information](https://thoughtworks.github.io/talisman/)

### Setup local env

Add this direnv to your shell as described [here](https://github.com/direnv/direnv/blob/master/docs/hook.md).

E.g. for ZSH add this to `~/.zshrc`

```bash
eval "$(direnv hook zsh)"
```

Create .env file

```bash
./run.sh env
```

**Note:** This needs to be repeated every time the secrets change

## Development

```bash
./run.sh dev
```

If you don't want to watch the log stream but let docker perform health checks until everything ist up, use detached mode:
```bash
./run.sh dev -d
```


The Application is available at http://127.0.0.1

This will start the backend [utilizing Spring Boot developer tools](https://docs.spring.io/spring-boot/docs/current/reference/html/using.html#using.devtools.restart) so changes in the Java sources will be reflected without manually restarting. Similarly, the frontend is served from [Vite](https://vitejs.dev) with [HMR](https://vitejs.dev/guide/features.html#hot-module-replacement).

To see logs of the containers, use e.g.

```bash
docker compose logs # for all
docker compose logs frontend # for specific services
```

To Stop the whole environment:

```bash
./run.sh down
```

Read the component individual documentation to figure out how to run them individually:

- [backend](./backend/README.md#development)
- [frontend](./frontend/README.md#development)

## Deployment

The pipeline performs the Deployment through GitOps using [ArgoCD](https://argoproj.github.io/cd/) (see [example pipeline deploy step definition](https://github.com/digitalservicebund/ris-backend-service/blob/main/.github/workflows/pipeline.yml#L657-L667)):

- Build and push the new Docker image (see here)
- Commit the new tag in the deployment manifest in the neuris-infra repository 
- Sync the respective ArgoCD App, which will cause ArgoCD to apply all changed Kubernetes manifests on the cluster to create the desired state

## Working with [Talisman](https://thoughtworks.github.io/talisman/)

Given you already know that a file that was edited, f.ex. `.github/workflows/pipeline.yml`, will require an updated checksum field in `.talismanrc`, execute the following command:

```bash
talisman -c .github/workflows/pipeline.yml | sed -n '3,5p' | yq e '.fileignoreconfig[0].checksum' - | tr -d '\r\n' | pbcopy
```

**Note that Talisman calculates the checksum based on contents in the index!**

What's left is to paste the new checksum into `.talismanrc`:

```diff
diff --git a/.talismanrc b/.talismanrc
index df6f840..de8d552 100644
--- a/.talismanrc
+++ b/.talismanrc
@@ -4,7 +4,7 @@ fileignoreconfig:
   - filename: build.gradle
     checksum: 3837e805b74b2251453049241cd7bd5e0f101d06769af7748344c09ef3d514b0
   - filename: .github/workflows/pipeline.yml
-    checksum: cb0bcce8968af031875bde690a48a60be440a67069c2b9aeffb4771a46abf9b9
+    checksum: e4518e01194712f64c54b62ec18b0c5745fe973a82390a336790674cdfe96e70
   - filename: .github/workflows/secrets-check.yml
     checksum: 836ea67ce7c67ecfb56519ff7f65ff461821cc221797f15c635108f212646664
   - filename: .github/workflows/scan.yml
```

## Architecture Decision Records

[Architecture decisions](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions) are kept in the [doc/adr](doc/adr) directory and are managed with [adr-tools](https://github.com/npryce/adr-tools).

## Slack notifications

Opt in to CI posting notifications for failing jobs to a particular Slack channel by setting a repository secret
with the name `SLACK_WEBHOOK_URL`, containing a url for [Incoming Webhooks](https://api.slack.com/messaging/webhooks).
