# 8. Deploy frontend in separate image

Date: 2022-11-03

## Status

Accepted

## Context

We have the commitment in our team to use distroless images, as they reduce the container size and improves the performance. These light weight images have lesser packages and therefore, reduces the attack surface and there are fewer components whcih can be vulnerable.

## Decision

We will be using the distroless nginx base image from chainguard, which is rebuilt every night from source and therefore always up to date.

## Consequences

In the past we spent a lot of time dealing with vulnerabilities, that occure in packages that our nginx alpine base image uses. As the distroless image depends on lesser packages, the chance of having vulerabilites is reduced.
