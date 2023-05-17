package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import lombok.Builder;

@Builder(toBuilder = true)
public record DocumentUnitNorm(
    String risAbbreviation, String singleNorm, Instant dateOfVersion, String dateOfRelevance) {}
