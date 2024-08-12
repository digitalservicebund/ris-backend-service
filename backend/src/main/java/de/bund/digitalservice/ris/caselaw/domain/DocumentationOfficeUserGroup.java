package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

// FIXME: Doesn't make sense. We rather want a Map<DocumentationOffice, Set<UserGroup>>
@Builder
public record DocumentationOfficeUserGroup(DocumentationOffice docOffice, String userGroup) {}
