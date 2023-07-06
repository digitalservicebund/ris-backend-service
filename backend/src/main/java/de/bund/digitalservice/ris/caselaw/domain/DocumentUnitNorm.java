package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import java.time.Instant;
import lombok.Builder;

@Builder(toBuilder = true)
public record DocumentUnitNorm(
    NormAbbreviation normAbbreviation,
    // @SingleNormConstraint
    String singleNorm,
    Instant dateOfVersion,
    String dateOfRelevance) {}
