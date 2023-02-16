package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import java.time.Instant;
import lombok.Builder;

@Builder
public record PreviousDecision(Long id, Court court, Instant date, String fileNumber) {}
