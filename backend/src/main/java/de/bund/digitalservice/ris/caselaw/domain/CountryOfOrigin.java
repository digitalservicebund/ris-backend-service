package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.UUID;
import lombok.Builder;

@Builder
public record CountryOfOrigin(
    UUID id,
    boolean newEntry,
    String legacyValue,
    FieldOfLaw country,
    FieldOfLaw fieldOfLaw,
    Long rank) {}
