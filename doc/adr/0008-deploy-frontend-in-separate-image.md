# 8. Deploy frontend in separate image

Date: 2022-09-02

## Status

Accepted

## Context

The original problem was, that we were not able to refresh our page or access direct links. When a users browser requests the initial request, the browser would make a GET request to our server, which by default loads our index.html file, which loads the vue router. When we click on any link on the page, vue router handles the request by pushing the state to the browser history, instead of making a GET request. Now, if we reload the page or access a link directly, the request will be send to the server, but since the routing library is not loaded yet, the server doesn’t know that path and responds with an error.

## Decision

We dissolve the built-in frontend to an independent container, so we can tell the nginx controller to send all api requests to the backend service and the rest to the frontend service, loading the index.html and let vue router do the magic. The argocd action deploys the two images to ArgoCD in two independent containers. The nginx webserver in the frontend container is configured to always serve the index.html for alle requests coming in to the frontend service.

We didn't move the frontend into it's own repo, because an SPA in a separate repository will need its own deployment (Nginx, S3 bucket etc.) and pipeline. Also it would not have access to Spring's out-of-the-box security configuration.

## Consequences

We are now able to reload the page or access the page via direct links. Also we don't need the workarounds in the e2e tests, to get to subpages, but can directly access them. The pipeline has 2 different jobs for building and pushing the frontend and backend image respectively, which can run in parallel and therefore fasten up the pipeline.
