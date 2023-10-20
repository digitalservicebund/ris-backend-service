package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitNorm;

public class DocumentUnitNormTransformer {
  private DocumentUnitNormTransformer() {}

  public static DocumentUnitNorm transformToDomain(NormReferenceDTO normDTO) {
    return DocumentUnitNorm.builder()
        .id(normDTO.getId())
        .normAbbreviation(NormAbbreviationTransformer.transformDTO(normDTO.getNormAbbreviation()))
        .singleNorm(normDTO.getSingleNorm())
        .dateOfVersion(normDTO.getDateOfVersion())
        .dateOfRelevance(normDTO.getDateOfRelevance())
        .build();
  }
}
