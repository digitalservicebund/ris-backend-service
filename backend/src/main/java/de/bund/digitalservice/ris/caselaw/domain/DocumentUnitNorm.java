package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
// to ignore the validationErrors that the frontend might be sending along
@JsonIgnoreProperties(ignoreUnknown = true)
public record DocumentUnitNorm(
    UUID id,
    NormAbbreviation normAbbreviation,
    String singleNorm,
    LocalDate dateOfVersion,
    String dateOfRelevance) {}
