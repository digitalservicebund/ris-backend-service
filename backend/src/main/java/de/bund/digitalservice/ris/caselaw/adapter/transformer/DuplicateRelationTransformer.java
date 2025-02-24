package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelation;
import de.bund.digitalservice.ris.caselaw.domain.PublicationStatus;
import de.bund.digitalservice.ris.caselaw.domain.Status;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import java.util.Optional;

public class DuplicateRelationTransformer {

  private DuplicateRelationTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  // transformToDTO not needed as data is never written by client

  /**
   * @param duplicateRelationDTO {@link DuplicateRelationDTO} of the duplicate
   * @param current {@link DocumentationUnitDTO} of the documentation unit that is currently
   *     requested
   */
  public static DuplicateRelation transformToDomain(
      DuplicateRelationDTO duplicateRelationDTO, DocumentationUnitDTO current) {
    DecisionDTO duplicate;
    if (duplicateRelationDTO.getDocumentationUnit1().getId().equals(current.getId())) {
      duplicate = duplicateRelationDTO.getDocumentationUnit2();
    } else {
      duplicate = duplicateRelationDTO.getDocumentationUnit1();
    }

    String courtLabel =
        Optional.ofNullable(duplicate.getCourt())
            .map(CourtTransformer::transformToDomain)
            .map(Court::label)
            .orElse(null);
    String firstFileNumber =
        duplicate.getFileNumbers().stream().findFirst().map(FileNumberDTO::getValue).orElse(null);
    String documentTypeLabel =
        Optional.ofNullable(duplicate.getDocumentType())
            .map(DocumentTypeDTO::getLabel)
            .orElse(null);
    PublicationStatus publicationStatus =
        Optional.ofNullable(duplicate.getStatus())
            .map(StatusTransformer::transformToDomain)
            .map(Status::publicationStatus)
            .orElse(null);

    return DuplicateRelation.builder()
        .documentNumber(duplicate.getDocumentNumber())
        .status(duplicateRelationDTO.getRelationStatus())
        .decisionDate(duplicate.getDate())
        .courtLabel(courtLabel)
        .documentType(documentTypeLabel)
        .publicationStatus(publicationStatus)
        .fileNumber(firstFileNumber)
        .isJdvDuplicateCheckActive(
            !Boolean.FALSE.equals(duplicate.getIsJdvDuplicateCheckActive())
                && !Boolean.FALSE.equals(current.getIsJdvDuplicateCheckActive()))
        .build();
  }
}
