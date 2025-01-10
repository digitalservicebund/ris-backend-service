package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitIdDuplicateCheckDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitIdDuplicateCheck;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DocumentationUnitIdDuplicateCheckTransformer {
  private DocumentationUnitIdDuplicateCheckTransformer() {}

  /**
   * @param documentationUnitIdDuplicateCheckDTO the reduced database representation of a
   *     documentation unit (duplicate)
   * @return DocumentationUnitIdDuplicateCheck the domain representation of a duplicate (reduced
   *     documentation unit)
   */
  public static DocumentationUnitIdDuplicateCheck transformToDomain(
      DocumentationUnitIdDuplicateCheckDTO documentationUnitIdDuplicateCheckDTO) {
    if (documentationUnitIdDuplicateCheckDTO == null) {
      return DocumentationUnitIdDuplicateCheck.builder().build();
    }

    return DocumentationUnitIdDuplicateCheck.builder()
        .isJdvDuplicateCheckActive(
            documentationUnitIdDuplicateCheckDTO.getIsJdvDuplicateCheckActive())
        .id(documentationUnitIdDuplicateCheckDTO.getId())
        .build();
  }
}
