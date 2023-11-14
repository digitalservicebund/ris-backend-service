package de.bund.digitalservice.ris.caselaw.domain;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentUnitSearchInput(
    UUID uuid,
    String documentNumberOrFileNumber,
    String courtType,
    String courtLocation,
    LocalDate decisionDate,
    LocalDate decisionDateEnd,
    DocumentationOffice documentationOffice,
    Status status,
    boolean myDocOfficeOnly) {}
