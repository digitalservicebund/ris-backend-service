package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DeviatingFileNumberDTO;
import de.bund.digitalservice.ris.caselaw.domain.DeviatingFileNumber;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DeviatingFileNumberTransformer {

  public static DeviatingFileNumberDTO transformToDTO(DeviatingFileNumber deviatingFileNumber) {
    return DeviatingFileNumberDTO.builder()
        .id(deviatingFileNumber.id())
        .value(deviatingFileNumber.fileNumber())
        .build();
  }

  public static DeviatingFileNumber transformToDomain(
      DeviatingFileNumberDTO DeviatingFileNumberDTO) {
    return DeviatingFileNumber.builder()
        .id(DeviatingFileNumberDTO.getId())
        .fileNumber(DeviatingFileNumberDTO.getValue())
        .build();
  }
}
