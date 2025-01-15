# 23. Use Spring MVC

Date: 2025-01-03

## Status

Accepted

## Context

The NeuRIS Caselaw backend has transitioned from using Spring WebFlux to Spring MVC. The decision to refactor was driven by the significant challenges encountered with WebFlux, as documented in [ADR 16](./0016-Identify-Reactive-WebFlux-as-Technical-Dept-in-Caselaw.md). These challenges included high cognitive complexity, limited library compatibility, and a lack of reactive backend requirements for the project.

Spring MVC was selected for its compatibility with JPA, extensive documentation, and the team's familiarity with its imperative programming model. The refactoring has been completed, and the backend is now fully implemented using Spring MVC.

## Decision

Spring MVC is adopted as the standard framework for backend development in the NeuRIS Caselaw project.

## Consequences

Benefits of the Decision:
- A simplified, readable codebase that aligns with the team's skill set.
- Full compatibility with the Spring ecosystem, including JPA, which enhances maintainability and consistency.
- Easier onboarding for new developers due to the widespread use and familiarity of Spring MVC.

Lessons Learned:
- Framework choices should align with both project needs and team expertise.
- A thorough assessment of long-term implications is critical when adopting a less familiar or niche technology.

Future Directions:
- Leverage Spring MVC’s ecosystem to accelerate feature development and enhance maintainability.