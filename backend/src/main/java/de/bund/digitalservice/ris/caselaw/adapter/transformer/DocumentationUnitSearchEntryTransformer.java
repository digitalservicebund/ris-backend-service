package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitSearchEntryDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchEntry;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.Status.StatusBuilder;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

public class DocumentationUnitSearchEntryTransformer {
  private DocumentationUnitSearchEntryTransformer() {}

  public static DocumentationUnitSearchEntry transferDTO(
      DocumentationUnitSearchEntryDTO searchEntryDTO) {
    return DocumentationUnitSearchEntry.builder()
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
    List<PublicationStatus> published =
        List.of(PublicationStatus.PUBLISHED, PublicationStatus.JURIS_PUBLISHED);

    StatusBuilder builder = Status.builder();
    if (publicationStatus != null && published.contains(publicationStatus)) {
      builder.publicationStatus(PublicationStatus.PUBLISHED);
    } else {
      builder.publicationStatus(publicationStatus);
    }

    builder.withError(Objects.requireNonNullElse(withError, false));

    return builder.build();
  }
}
