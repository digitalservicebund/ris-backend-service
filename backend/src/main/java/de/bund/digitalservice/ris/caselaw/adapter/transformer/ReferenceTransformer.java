package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.Reference;

public class ReferenceTransformer {

  public static Reference transformToDomain(ReferenceDTO referenceDTO) {
    return Reference.builder()
        .uuid(referenceDTO.getId())
        .referenceSupplement(referenceDTO.getReferenceSupplement())
        .legalPeriodical(
            LegalPeriodicalTransformer.transformToDomain(referenceDTO.getLegalPeriodical()))
        .citation(referenceDTO.getCitation())
        .footnote(referenceDTO.getFootnote())
        .build();
  }

  public static ReferenceDTO transformToDTO(Reference reference) {
    return ReferenceDTO.builder()
        .id(reference.uuid())
        .referenceSupplement(reference.referenceSupplement())
        .legalPeriodical(
            reference.legalPeriodical() == null
                    || reference.legalPeriodical().legalPeriodicalId() == null
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
