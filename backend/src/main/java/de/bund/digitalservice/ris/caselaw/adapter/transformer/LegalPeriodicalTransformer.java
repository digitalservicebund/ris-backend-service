package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LegalPeriodicalTransformer {
  private LegalPeriodicalTransformer() {}

  public static LegalPeriodical transformToDomain(LegalPeriodicalDTO legalPeriodicalDTO) {
    if (legalPeriodicalDTO == null) {
      return null;
    }

    return LegalPeriodical.builder()
        .legalPeriodicalId(legalPeriodicalDTO.getId())
        .legalPeriodicalAbbreviation(legalPeriodicalDTO.getAbbreviation())
        .legalPeriodicalTitle(legalPeriodicalDTO.getTitle())
        .legalPeriodicalSubtitle(legalPeriodicalDTO.getSubtitle())
        .primaryReference(legalPeriodicalDTO.getPrimaryReference())
        .citationStyle(legalPeriodicalDTO.getCitationStyle())
        .build();
  }

  public static LegalPeriodicalDTO transformToDTO(LegalPeriodical legalPeriodical) {
    if (legalPeriodical == null) {
      return null;
    }

    return LegalPeriodicalDTO.builder()
        .id(legalPeriodical.legalPeriodicalId())
        .title(legalPeriodical.legalPeriodicalTitle())
        .abbreviation(legalPeriodical.legalPeriodicalAbbreviation())
        .subtitle(legalPeriodical.legalPeriodicalSubtitle())
        .citationStyle(legalPeriodical.citationStyle())
        .primaryReference(legalPeriodical.primaryReference())
        .build();
  }
}
