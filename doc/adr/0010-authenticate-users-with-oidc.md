# 10. Authenticate users with OIDC

Date: 2023-02-10

## Status

Accepted

## Context

Until recently and as a temporary solution, we restricted access to our application with basic auth. The credentials were shared among our test users and the team. For future features we will need role-based access control (RBAC) and therefore, user-specific logins. Also, users might want to have a personalized view of the system, e.g. see which document units they have been working on recently etc.. Also, for security reasons it is essential to be able to set and reset passwords individually.

## Decision

We decided to use OIDC (OpenID Connect) for user authentication as this is the industry standard. It is supported by many libraries, such as Spring Boot Security, and therefore easy to implement. We use hosted keycloak instances as resource servers and for self-service flows (forgot password, login, etc.).

## Consequences

Each team member and end user logs in with their own credentials. This creates friction for a short period of time. The end users were informed in advance in order to reduce that. 

In order for OIDC to work, the local setup needed both frontend and backend to use the same port and therefore, a traefik router was added to the setup. Since the backend component has to be stateless in order to scale, a redis database was setup in all environments to hold the sessions.

As a consequence, the end-2-end tests, that previously used basic auth, had to be updated. The Readme documents were updated as well.

