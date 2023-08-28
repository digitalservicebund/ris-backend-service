package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
public record Procedure(String label, Integer documentUnitCount) {}
