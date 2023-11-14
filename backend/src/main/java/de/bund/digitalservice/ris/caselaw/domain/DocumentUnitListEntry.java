package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentUnitListEntry(
    UUID uuid,
    String documentNumber,
    LocalDate decisionDate,
    String fileName,
    String fileNumber,
    DocumentType documentType,
    Court court,
    DocumentationOffice documentationOffice,
    Status status) {}
