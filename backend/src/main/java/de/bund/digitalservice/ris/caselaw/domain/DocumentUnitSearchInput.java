package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentUnitSearchInput(
    UUID uuid,
    String documentNumberOrFileNumber,
    Court court,
    Instant decisionDate,
    DocumentationOffice documentationOffice,
    DocumentUnitStatus status,
    boolean myDocOfficeOnly) {}
