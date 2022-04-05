# 2. Use Gradle as build tool

Date: 2021-10-06

## Status

Accepted

## Context

We need to stick to a single build tool to avoid friction that is stemming from forcing engineers
to (re)learn a new build tool on each new project.

Furthermore, a build tool has be flexible, customizable and fast.

## Decision

We will use [Gradle](https://gradle.org) as build tool.

## Consequences

Gradle [performs better compared to Maven](https://gradle.org/gradle-vs-maven-performance/), thus we
may spend less time waiting for the build.

Furthermore it is easily customizable as build script is code and as it was modeled with extensibility
in mind; its approach to dependency management is superior; separation of unit and integration tests
can be accomplished out of the box.

In order for using the Gradle DSL effectively, at least a basic understanding of Groovy is required (switching
to a Kotlin based DSL would be possible if this may reduce overhead).

We might loose out on certain Maven-based functionality/integrations.
