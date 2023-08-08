package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentUnitListEntry(
    UUID uuid,
    String documentNumber,
    Instant decisionDate,
    DataSource dataSource,
    String fileName,
    String fileNumber,
    DocumentType documentType,
    Court court,
    DocumentationOffice documentationOffice,
    DocumentUnitStatus status) {}
