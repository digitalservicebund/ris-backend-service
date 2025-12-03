package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NonApplicationNormDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm;
import de.bund.digitalservice.ris.caselaw.domain.SingleNorm.SingleNormBuilder;
import java.time.LocalDate;
import java.util.UUID;

public class SingleNormTransformer {

  private SingleNormTransformer() {}

  /**
   * Transforms a {@link NormReferenceDTO} object into a SingleNorm domain object. Each
   * NormReferenceDTO holds the information of exact one single norm, whereas the {@link
   * de.bund.digitalservice.ris.caselaw.domain.NormReference} domain object holds a list of {@link
   * SingleNorm}, that belong to the same norm abbreviation.
   *
   * @param normReferenceDTO The NormReferenceDTO object to be transformed.
   * @return The SingleNorm domain object representing the transformed NormReferenceDTO.
   */
  public static SingleNorm transformToDomain(NormReferenceDTO normReferenceDTO) {
    if (normReferenceDTO.isSingleNormEmpty()) {
      return null;
    }

    SingleNormBuilder builder =
        transformToDomain(
            normReferenceDTO.getId(),
            normReferenceDTO.getSingleNorm(),
            normReferenceDTO.getDateOfRelevance(),
            normReferenceDTO.getDateOfVersion());

    if (normReferenceDTO.getLegalForce() != null) {
      builder.legalForce(LegalForceTransformer.transformToDomain(normReferenceDTO.getLegalForce()));
    }

    return builder.build();
  }

  /**
   * Transforms a {@link NonApplicationNormDTO} object into a SingleNorm domain object.
   *
   * @param nonApplicationNormDTO The NormReferenceDTO object to be transformed.
   * @return The SingleNorm domain object representing the transformed NonApplicationNormDTO.
   */
  public static SingleNorm transformToDomain(NonApplicationNormDTO nonApplicationNormDTO) {
    if (nonApplicationNormDTO.isSingleNormEmpty()) {
      return null;
    }

    return transformToDomain(
            nonApplicationNormDTO.getId(),
            nonApplicationNormDTO.getSingleNorm(),
            nonApplicationNormDTO.getDateOfRelevance(),
            nonApplicationNormDTO.getDateOfVersion())
        .build();
  }

  private static SingleNorm.SingleNormBuilder transformToDomain(
      UUID id, String singleNorm, String dateOfRelevance, LocalDate dateOfVersion) {
    return SingleNorm.builder()
        .id(id)
        .singleNorm(singleNorm)
        .dateOfRelevance(dateOfRelevance)
        .dateOfVersion(dateOfVersion);
  }
}
