package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.Reference;

public class ReferenceTransformer {

  public static Reference transformToDomain(ReferenceDTO referenceDTO) {
    return Reference.builder()
        .referenceSupplement(referenceDTO.getReferenceSupplement())
        .legalPeriodicalId(referenceDTO.getLegalPeriodical().getId())
        .legalPeriodicalAbbreviation(referenceDTO.getLegalPeriodical().getAbbreviation())
        .legalPeriodicalTitle(referenceDTO.getLegalPeriodical().getTitle())
        .legalPeriodicalSubtitle(referenceDTO.getLegalPeriodical().getSubtitle())
        .primaryReference(referenceDTO.getLegalPeriodical().getPrimaryReference())
        .citation(referenceDTO.getCitation())
        .footnote(referenceDTO.getFootnote())
        .id(referenceDTO.getId())
        .build();
  }

  public static ReferenceDTO transformToDTO(Reference reference) {
    return ReferenceDTO.builder()
        .id(reference.id())
        .referenceSupplement(reference.referenceSupplement())
        .legalPeriodical(LegalPeriodicalDTO.builder().id(reference.legalPeriodicalId()).build())
        .citation(reference.citation())
        .footnote(reference.footnote())
        .legalPeriodicalRawValue(reference.legalPeriodicalAbbreviation())
        .build();
  }

  private ReferenceTransformer() {
    // utility class
  }
}
