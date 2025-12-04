package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.AbstractNormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm.SingleNormBuilder;

public class SingleNormTransformer {

  private SingleNormTransformer() {}

  /**
   * Transforms a {@link AbstractNormReferenceDTO} object into a SingleNorm domain object. Each
   * NormReferenceDTO holds the information of exact one single norm, whereas the {@link
   * de.bund.digitalservice.ris.caselaw.domain.NormReference} domain object holds a list of {@link
   * SingleNorm}, that belong to the same norm abbreviation.
   *
   * @param normReferenceDTO The AbstractNormReferenceDTO object to be transformed.
   * @return The SingleNorm domain object representing the transformed NormReferenceDTO.
   */
  public static SingleNorm transformToDomain(AbstractNormReferenceDTO normReferenceDTO) {
    if (normReferenceDTO.isSingleNormEmpty()) {
      return null;
    }

    SingleNormBuilder builder =
        SingleNorm.builder()
            .id(normReferenceDTO.getId())
            .singleNorm(normReferenceDTO.getSingleNorm())
            .dateOfRelevance(normReferenceDTO.getDateOfRelevance())
            .dateOfVersion(normReferenceDTO.getDateOfVersion());

    if (normReferenceDTO.getLegalForce() != null) {
      builder.legalForce(LegalForceTransformer.transformToDomain(normReferenceDTO.getLegalForce()));
    }

    return builder.build();
  }
}
