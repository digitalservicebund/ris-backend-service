package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentUnitSearchInput(
    UUID uuid,
    String documentNumberOrFileNumber,
    String courtType,
    String courtLocation,
    Instant decisionDate,
    Instant decisionDateEnd,
    DocumentationOffice documentationOffice,
    DocumentUnitStatus status,
    boolean myDocOfficeOnly) {}
