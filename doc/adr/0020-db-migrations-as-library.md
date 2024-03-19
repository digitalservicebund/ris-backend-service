# 20. db-migrations-as-library

Date: 2024-03-19

## Status

Proposed

## Context

Right now, we have migration scripts in two different repositories: [`ris-backend-service`](https://github.com/digitalservicebund/ris-backend-service) and [`ris-data-migration`](https://github.com/digitalservicebund/ris-data-migration). Although `ris-backend-service` uses [flyway](https://flywaydb.org/) to manage database changes, it doesn't have all the scripts needed to set up the database from scratch.

Also, the `ris-data-migration` scripts contain comments that must not be made public due to copyright reasons.

This situation is causing several issues:

1. It's confusing. (New) developers find it hard to set up their local development environment. Also it's often not intuitive where to put new migrations.
2. Integration tests need to duplicate the migration scripts from `ris-data-migration` (see [`create_migration_scheme_and_extensions.sql`](https://github.com/digitalservicebund/ris-backend-service/blob/03f02f302b0b8acda78bfaccffa9354859367152/backend/src/test/resources/db/create_migration_scheme_and_extensions.sql))

## Decision

We will leverage the [gradle multi-project setup](https://docs.gradle.org/current/userguide/intro_multi_project_builds.html) of `ris-data-migration`, add a new sub project and make a private library of the migration scripts. The `ris-backend-service` will use this library.

## Consequences

All migration scripts will be in one place, making it easier for both projects to access the scripts needed to set up the database correctly.

Working with database migrations will involve more steps, like adding a script to `ris-data-migration`, releasing a new library version, and updating ris-backend-service to use this new version.

Because `ris-data-migration` is private, the local development environment as well as CI/CD will become more complex. However, we already solved the same issue for the [`neuris-juris-xml-export`](https://github.com/digitalservicebund/neuris-juris-xml-export) and can probably use the same solution.
