package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitNormDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitNorm;

public class DocumentUnitNormTransformer {
  private DocumentUnitNormTransformer() {}

  public static DocumentUnitNorm transformToDomain(DocumentUnitNormDTO normDTO) {
    return DocumentUnitNorm.builder()
        .risAbbreviation(normDTO.getRisAbbreviation())
        .singleNorm(normDTO.getSingleNorm())
        .dateOfVersion(normDTO.getDateOfVersion())
        .dateOfRelevance(normDTO.getDateOfRelevance())
        .build();
  }
}
