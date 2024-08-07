package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.Reference;

public class ReferenceTransformer {

  public static Reference transformToDomain(ReferenceDTO referenceDTO) {
    return Reference.builder()
        .uuid(referenceDTO.getId())
        .referenceSupplement(referenceDTO.getReferenceSupplement())
        .legalPeriodical(
            LegalPeriodicalTransformer.transformToDomain(referenceDTO.getLegalPeriodical()))
        .legalPeriodicalRawValue(referenceDTO.getLegalPeriodicalRawValue())
        .citation(referenceDTO.getCitation())
        .footnote(referenceDTO.getFootnote())
        .build();
  }

  public static ReferenceDTO transformToDTO(Reference reference) {
    return ReferenceDTO.builder()
        .id(reference.uuid())
        .referenceSupplement(reference.referenceSupplement())
        .legalPeriodical(LegalPeriodicalTransformer.transformToDTO(reference.legalPeriodical()))
        .citation(reference.citation())
        .footnote(reference.footnote())
        .legalPeriodicalRawValue(reference.legalPeriodical().legalPeriodicalAbbreviation())
        .build();
  }

  private ReferenceTransformer() {
    // utility class
  }
}
