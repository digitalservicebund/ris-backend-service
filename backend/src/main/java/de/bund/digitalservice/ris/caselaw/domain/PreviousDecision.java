package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import lombok.Builder;

@Builder
public record PreviousDecision(
    Long id, String courtType, String courtPlace, Instant date, String fileNumber) {}
