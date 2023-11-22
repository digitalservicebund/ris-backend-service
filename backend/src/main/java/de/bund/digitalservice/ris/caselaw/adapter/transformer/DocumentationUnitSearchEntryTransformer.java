package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitSearchEntryDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchResult;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.Status.StatusBuilder;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

public class DocumentationUnitSearchEntryTransformer {
  private DocumentationUnitSearchEntryTransformer() {}

  public static DocumentationUnitSearchResult transferDTO(
      DocumentationUnitSearchEntryDTO searchEntryDTO) {
    return DocumentationUnitSearchResult.builder()
        .uuid(searchEntryDTO.getUuid())
        .documentNumber(searchEntryDTO.getDocumentNumber())
        .court(
            Court.builder()
                .type(searchEntryDTO.getCourtType())
                .location(searchEntryDTO.getCourtLocation())
                .build())
        .fileNumber(searchEntryDTO.getFirstFileNumber())
        .fileName(searchEntryDTO.getFileName())
        .decisionDate(
            LocalDate.ofInstant(searchEntryDTO.getDecisionDate(), ZoneId.of("Europe/Berlin")))
        .documentType(DocumentType.builder().label(searchEntryDTO.getDocumentType()).build())
        .status(
            getPublicStatus(searchEntryDTO.getPublicationStatus(), searchEntryDTO.getWithError()))
        .build();
  }

  private static Status getPublicStatus(PublicationStatus publicationStatus, Boolean withError) {
    StatusBuilder builder = Status.builder();
    builder.publicationStatus(publicationStatus);
    builder.withError(Objects.requireNonNullElse(withError, false));
    return builder.build();
  }
}
