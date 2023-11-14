package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
public record DocumentationOffice(String label, String abbreviation) {}
