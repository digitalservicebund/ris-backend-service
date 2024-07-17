package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.Reference;

public class ReferenceTransformer {

  public static Reference transformToDomain(ReferenceDTO referenceDTO) {
    return Reference.builder()
        .referenceSupplement(referenceDTO.getReferenceSupplement())
        .legalPeriodicalId(
            referenceDTO.getLegalPeriodical() == null
                ? null
                : referenceDTO.getLegalPeriodical().getId())
        .legalPeriodicalAbbreviation(
            referenceDTO.getLegalPeriodical() == null
                ? referenceDTO.getLegalPeriodicalRawValue()
                : referenceDTO.getLegalPeriodical().getAbbreviation())
        .legalPeriodicalTitle(
            referenceDTO.getLegalPeriodical() == null
                ? null
                : referenceDTO.getLegalPeriodical().getTitle())
        .legalPeriodicalSubtitle(
            referenceDTO.getLegalPeriodical() == null
                ? null
                : referenceDTO.getLegalPeriodical().getSubtitle())
        .primaryReference(
            referenceDTO.getLegalPeriodical() == null
                ? (referenceDTO.getType() != null && referenceDTO.getType().equals("amtlich"))
                : referenceDTO.getLegalPeriodical().getPrimaryReference())
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
            reference.legalPeriodicalId() == null
                ? null
                : LegalPeriodicalDTO.builder().id(reference.legalPeriodicalId()).build())
        .citation(reference.citation())
        .footnote(reference.footnote())
        .legalPeriodicalRawValue(reference.legalPeriodicalAbbreviation())
        .type(reference.primaryReference() ? "amtlich" : "nichtamtlich")
        .build();
  }

  private ReferenceTransformer() {
    // utility class
  }
}
