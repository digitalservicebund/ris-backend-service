package de.bund.digitalservice.ris.caselaw.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentationUnitSearchInput(
    UUID uuid,
    String documentNumber,
    String fileNumber,
    String courtType,
    String courtLocation,
    LocalDate decisionDate,
    LocalDate decisionDateEnd,
    LocalDateTime publicationDate,
    Boolean onlyScheduled,
    DocumentationOffice documentationOffice,
    Status status,
    boolean myDocOfficeOnly) {}
