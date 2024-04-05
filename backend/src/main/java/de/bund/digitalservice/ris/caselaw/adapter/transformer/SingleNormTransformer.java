package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;

public class SingleNormTransformer {

  public static SingleNorm transformToDomain(NormReferenceDTO normReferenceDTO) {
    return SingleNorm.builder()
        .singleNorm(normReferenceDTO.getSingleNorm())
        .dateOfRelevance(normReferenceDTO.getDateOfRelevance())
        .dateOfVersion(normReferenceDTO.getDateOfVersion())
        .build();
  }
}
