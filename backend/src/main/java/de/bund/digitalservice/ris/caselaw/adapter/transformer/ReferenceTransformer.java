package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.Reference;

public class ReferenceTransformer {

  public static Reference transformToDomain(ReferenceDTO referenceDTO) {
    return Reference.builder()
        .referenceSupplement(referenceDTO.getReferenceSupplement())
        .legalPeriodical(
            LegalPeriodicalTransformer.transformToDomain(referenceDTO.getLegalPeriodical()))
        .citation(referenceDTO.getCitation())
        .footnote(referenceDTO.getFootnote())
        .id(referenceDTO.getId())
        .build();
  }

  public static ReferenceDTO transformToDTO(Reference reference) {
    return ReferenceDTO.builder()
        .id(reference.id())
        .referenceSupplement(reference.referenceSupplement())
        .legalPeriodical(
            reference.legalPeriodical().legalPeriodicalId() == null
                ? null
                : LegalPeriodicalDTO.builder()
                    .id(reference.legalPeriodical().legalPeriodicalId())
                    .build())
        .citation(reference.citation())
        .footnote(reference.footnote())
        .legalPeriodicalRawValue(reference.legalPeriodical().legalPeriodicalAbbreviation())
        .build();
  }

  private ReferenceTransformer() {
    // utility class
  }
}
