package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationDTO;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelation;

public class DuplicateRelationTransformer {

  private DuplicateRelationTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  // transformToDTO not needed as data is never written by client

  /**
   * Current is the docUnit that we are currently requesting -> the other doc unit is its duplicate.
   */
  public static DuplicateRelation transformToDomain(
      DuplicateRelationDTO duplicateRelationDTO, DocumentationUnitDTO current) {
    DocumentationUnitDTO duplicate;
    if (duplicateRelationDTO.getDocumentationUnit1().getId().equals(current.getId())) {
      duplicate = duplicateRelationDTO.getDocumentationUnit2();
    } else {
      duplicate = duplicateRelationDTO.getDocumentationUnit1();
    }

    return DuplicateRelation.builder()
        .documentNumber(duplicate.getDocumentNumber())
        .status(duplicateRelationDTO.getStatus())
        .build();
  }
}
