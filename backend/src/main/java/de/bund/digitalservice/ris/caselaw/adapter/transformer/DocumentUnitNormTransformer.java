package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.NormReference;

public class DocumentUnitNormTransformer {
  private DocumentUnitNormTransformer() {}

  public static NormReference transformToDomain(NormReferenceDTO normDTO) {
    return NormReference.builder()
        .id(normDTO.getId())
        .normAbbreviation(NormAbbreviationTransformer.transformDTO(normDTO.getNormAbbreviation()))
        .singleNorm(normDTO.getSingleNorm())
        .dateOfVersion(normDTO.getDateOfVersion())
        .dateOfRelevance(normDTO.getDateOfRelevance())
        .build();
  }
}
