package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitSearchEntryDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus.DocumentUnitStatusBuilder;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitSearchEntry;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

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

  private static DocumentUnitStatus getPublicStatus(
      PublicationStatus publicationStatus, Boolean withError) {
    List<PublicationStatus> published =
        List.of(PublicationStatus.PUBLISHED, PublicationStatus.JURIS_PUBLISHED);

    DocumentUnitStatusBuilder builder = DocumentUnitStatus.builder();
    if (publicationStatus != null && published.contains(publicationStatus)) {
      builder.publicationStatus(PublicationStatus.PUBLISHED);
    } else {
      builder.publicationStatus(publicationStatus);
    }

    if (withError == null) {
      builder.withError(false);
    } else {
      builder.withError(withError);
    }
    return builder.build();
  }
}
