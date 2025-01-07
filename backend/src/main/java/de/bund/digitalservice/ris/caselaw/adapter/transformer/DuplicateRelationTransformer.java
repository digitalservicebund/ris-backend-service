package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DuplicateRelationDTO;
import de.bund.digitalservice.ris.caselaw.domain.DuplicateRelation;

public class DuplicateRelationTransformer {

  private DuplicateRelationTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  // transformToDTO not needed as data is never written

  public static DuplicateRelation transformToDomain(DuplicateRelationDTO duplicateRelationDTO) {
    System.out.println(duplicateRelationDTO.getDocumentationUnit1().getDocumentNumber());
    System.out.println(duplicateRelationDTO.getDocumentationUnit2().getDocumentNumber());

    System.out.println("HIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIER");

    // TODO: What do we want to expose? Doc Number enough?
    // Should we only keep the data of the other docUnit? (Otherwise, we need to filter in the FE)

    return DuplicateRelation.builder()
        .docUnitId1(duplicateRelationDTO.getId().getDocumentationUnitId1())
        .docUnitId2(duplicateRelationDTO.getId().getDocumentationUnitId2())
        .status(duplicateRelationDTO.getStatus())
        .build();
  }
}
