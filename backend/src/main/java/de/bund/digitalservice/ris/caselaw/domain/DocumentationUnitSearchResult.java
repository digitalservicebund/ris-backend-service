package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
public record DocumentationUnitSearchResult(
    UUID uuid,
    String documentNumber,
    Court court,
    String fileNumber,
    String fileName,
    LocalDate decisionDate,
    String appraisalBody,
    DocumentType documentType,
    UUID referencedDocumentationUnitId,
    Status status) {}
