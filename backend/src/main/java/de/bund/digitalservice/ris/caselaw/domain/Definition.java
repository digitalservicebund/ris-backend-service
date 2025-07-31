package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Definition(
    UUID id, boolean newEntry, String definedTerm, Long definingBorderNumber) {}
