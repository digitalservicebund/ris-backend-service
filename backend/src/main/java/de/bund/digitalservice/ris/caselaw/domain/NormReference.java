package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder(toBuilder = true)
// to ignore the validationErrors that the frontend might be sending along
@JsonIgnoreProperties(ignoreUnknown = true)
public record NormReference(
    UUID id,
    NormAbbreviation normAbbreviation,
    String normAbbreviationRawValue,
    List<SingleNorm> singleNorms) {}
