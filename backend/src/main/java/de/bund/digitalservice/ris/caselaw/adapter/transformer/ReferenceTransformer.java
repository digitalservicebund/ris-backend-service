package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;

public class ReferenceTransformer {

  public static Reference transformToDomain(ReferenceDTO referenceDTO) {
    LegalPeriodical legalPeriodical = null;

    if (referenceDTO.getLegalPeriodical() != null) {
      legalPeriodical =
          LegalPeriodicalTransformer.transformToDomain(referenceDTO.getLegalPeriodical());
    }

    return Reference.builder()
        .uuid(referenceDTO.getId())
        .referenceSupplement(referenceDTO.getReferenceSupplement())
        .legalPeriodical(legalPeriodical)
        .legalPeriodicalRawValue(referenceDTO.getLegalPeriodicalRawValue())
        .citation(referenceDTO.getCitation())
        .footnote(referenceDTO.getFootnote())
        .build();
  }

  public static ReferenceDTO transformToDTO(Reference reference) {
    LegalPeriodicalDTO legalPeriodicalDTO = null;

    if (reference.legalPeriodical() != null) {
      legalPeriodicalDTO = LegalPeriodicalTransformer.transformToDTO(reference.legalPeriodical());
    }
    return ReferenceDTO.builder()
        .id(reference.uuid())
        .referenceSupplement(reference.referenceSupplement())
        .legalPeriodical(legalPeriodicalDTO)
        .citation(reference.citation())
        .footnote(reference.footnote())
        .legalPeriodicalRawValue(reference.legalPeriodicalRawValue())
        .build();
  }

  private ReferenceTransformer() {
    // utility class
  }
}
