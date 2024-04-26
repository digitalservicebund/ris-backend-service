package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalForceTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceType;

public class LegalForceTypeTransformer {
  private LegalForceTypeTransformer() {}

  public static LegalForceType transformDTO(LegalForceTypeDTO dto) {
    if (dto == null) {
      return null;
    }

    return LegalForceType.builder()
        .id(dto.getId())
        .abbreviation(dto.getAbbreviation())
        .label(dto.getLabel())
        .build();
  }
}
