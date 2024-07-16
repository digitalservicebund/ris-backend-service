package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodical;
import de.bund.digitalservice.ris.caselaw.domain.Reference;

public class ReferenceTransformer {

  public static Reference transformToDomain(ReferenceDTO referenceDTO) {
    return Reference.builder()
        .rank(referenceDTO.getRank())
        .referenceSupplement(referenceDTO.getReferenceSupplement())
        .legalPeriodical(
            LegalPeriodical.builder()
                .id(referenceDTO.getLegalPeriodical().getId())
                .abbreviation(referenceDTO.getLegalPeriodical().getAbbreviation())
                .title(referenceDTO.getLegalPeriodical().getTitle())
                .subtitle(referenceDTO.getLegalPeriodical().getSubtitle())
                .primaryReference(referenceDTO.getLegalPeriodical().getPrimaryReference())
                .citationStyle(referenceDTO.getLegalPeriodical().getCitationStyle())
                .build())
        .citation(referenceDTO.getCitation())
        .footnote(referenceDTO.getFootnote())
        .build();
  }

  public static ReferenceDTO transformToDTO(Reference reference) {
    return ReferenceDTO.builder()
        .rank(reference.rank())
        .referenceSupplement(reference.referenceSupplement())
        .legalPeriodical(
            LegalPeriodicalDTO.builder()
                .id(reference.legalPeriodical().id())
                .abbreviation(reference.legalPeriodical().abbreviation())
                .title(reference.legalPeriodical().title())
                .subtitle(reference.legalPeriodical().subtitle())
                .primaryReference(reference.legalPeriodical().primaryReference())
                .citationStyle(reference.legalPeriodical().citationStyle())
                .build())
        .citation(reference.citation())
        .footnote(reference.footnote())
        .build();
  }

  private ReferenceTransformer() {
    // utility class
  }
}
