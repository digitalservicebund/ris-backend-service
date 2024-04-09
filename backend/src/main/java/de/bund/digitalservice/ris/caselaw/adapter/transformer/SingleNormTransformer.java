package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;

public class SingleNormTransformer {

  public static SingleNorm transformToDomain(NormReferenceDTO normReferenceDTO) {
    if (normReferenceDTO.getSingleNorm() == null) {
      return null;
    }

    return SingleNorm.builder()
        .id(normReferenceDTO.getId())
        .singleNorm(normReferenceDTO.getSingleNorm())
        .dateOfRelevance(normReferenceDTO.getDateOfRelevance())
        .dateOfVersion(normReferenceDTO.getDateOfVersion())
        .build();
  }
}
