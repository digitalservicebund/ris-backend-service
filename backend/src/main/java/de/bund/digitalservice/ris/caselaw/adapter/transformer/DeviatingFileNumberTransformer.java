package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.domain.DeviatingFileNumber;

public class DeviatingFileNumberTransformer {

  public static DeviatingFileNumberDTO transformToDTO(
      DeviatingFileNumber deviatingFileNumber, Long rank) {
    return DeviatingFileNumberDTO.builder()
        .value(deviatingFileNumber.fileNumber())
        .rank(rank)
        .build();
  }

  public static DeviatingFileNumber transformToDomain(
      DeviatingFileNumberDTO DeviatingFileNumberDTO) {
    return DeviatingFileNumber.builder().fileNumber(DeviatingFileNumberDTO.getValue()).build();
  }
}
