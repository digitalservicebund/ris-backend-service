package de.bund.digitalservice.ris.caselaw.domain.textcheck;

import lombok.Builder;

@Builder
public record Rule(String id, String description, String issueType, Category category) {}
