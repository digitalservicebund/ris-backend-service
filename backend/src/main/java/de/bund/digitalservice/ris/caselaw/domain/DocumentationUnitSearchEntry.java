package de.bund.digitalservice.ris.caselaw.domain;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentationUnitSearchEntry(
    UUID uuid,
    String documentNumber,
    String courtType,
    String courtLocation,
    String fileNumber,
    String fileName,
    Instant decisionDate,
    String documentType,
    DocumentUnitStatus status) {}
