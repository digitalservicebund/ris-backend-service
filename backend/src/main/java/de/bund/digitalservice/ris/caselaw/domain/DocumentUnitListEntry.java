package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentUnitListEntry(
    UUID uuid,
    String documentNumber,
    Instant creationTimestamp,
    DataSource dataSource,
    String fileName,
    String fileNumber,
    DocumentType documentType,
    DocumentationOffice documentationOffice,
    DocumentUnitStatus status) {}
