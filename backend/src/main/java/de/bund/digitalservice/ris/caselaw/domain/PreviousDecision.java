package de.bund.digitalservice.ris.caselaw.domain;

import lombok.Builder;

@Builder
public record PreviousDecision(
    Long id, String courtType, String courtPlace, String date, String fileNumber) {}
