package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitSearchEntryDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchResult;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.Status.StatusBuilder;
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
        .courtType(searchEntryDTO.getCourtType())
        .courtLocation(searchEntryDTO.getCourtLocation())
        .fileNumber(searchEntryDTO.getFirstFileNumber())
        .fileName(searchEntryDTO.getFileName())
        .decisionDate(
            LocalDate.ofInstant(searchEntryDTO.getDecisionDate(), ZoneId.of("Europe/Berlin")))
        .documentType(searchEntryDTO.getDocumentType())
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
