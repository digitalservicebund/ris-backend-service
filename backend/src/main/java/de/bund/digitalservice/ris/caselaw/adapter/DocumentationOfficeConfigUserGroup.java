package de.bund.digitalservice.ris.caselaw.adapter;

import lombok.Builder;

@Builder
public record DocumentationOfficeConfigUserGroup(
    String userGroupPathName, String docOfficeAbbreviation, boolean isInternal) {}
