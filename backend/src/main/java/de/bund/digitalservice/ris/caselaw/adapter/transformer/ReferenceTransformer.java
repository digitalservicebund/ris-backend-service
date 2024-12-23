package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReferenceTransformer {
  public static Reference transformToDomain(ReferenceDTO referenceDTO) {
    if (referenceDTO == null) {
      return null;
    }
    LegalPeriodical legalPeriodical = null;

    if (referenceDTO.getLegalPeriodical() != null) {
      legalPeriodical =
          LegalPeriodicalTransformer.transformToDomain(referenceDTO.getLegalPeriodical());
    }

    Boolean isPrimaryReference =
        legalPeriodical != null
            ? legalPeriodical.primaryReference()
            : referenceDTO.getType() != null
                ? referenceDTO.getType().equals("amtlich") // fallback to raw value
                : null;

    return Reference.builder()
        .id(referenceDTO.getId())
        .referenceSupplement(referenceDTO.getReferenceSupplement())
        .citation(referenceDTO.getCitation())
        .footnote(referenceDTO.getFootnote())
        .referenceType(ReferenceType.CASELAW)
        .documentationUnit(
            RelatedDocumentationUnitTransformer.transformToDomain(
                referenceDTO.getDocumentationUnit()))
        // editionRank is only set if reference is requested from edition
        .rank(
            referenceDTO.getEditionRank() != null
                ? referenceDTO.getEditionRank()
                : referenceDTO.getRank())
        .legalPeriodical(legalPeriodical)
        .legalPeriodicalRawValue(
            legalPeriodical != null
                ? legalPeriodical.abbreviation()
                : referenceDTO.getLegalPeriodicalRawValue()) // fallback to raw value
        .primaryReference(isPrimaryReference)
        .build();
  }

  public static ReferenceDTO transformToDTO(Reference reference) {
    LegalPeriodicalDTO legalPeriodicalDTO = null;
    String legalPeriodicalRawValue = null;

    if (reference.legalPeriodical() != null) {
      legalPeriodicalDTO = LegalPeriodicalTransformer.transformToDTO(reference.legalPeriodical());
      legalPeriodicalRawValue = reference.legalPeriodical().abbreviation();
    }

    DocumentationUnitDTO documentationUnitDTO = null;
    if (reference.documentationUnit() != null) {
      documentationUnitDTO =
          DocumentationUnitDTO.builder().id(reference.documentationUnit().getUuid()).build();
    }

    boolean isPrimaryReference =
        legalPeriodicalDTO != null
            ? legalPeriodicalDTO.getPrimaryReference()
            : reference.primaryReference() != null
                && reference.primaryReference(); // fallback to nichtamtlich

    return ReferenceDTO.builder()
        .id(reference.id())
        .referenceSupplement(reference.referenceSupplement())
        .legalPeriodical(legalPeriodicalDTO)
        .citation(reference.citation())
        .footnote(reference.footnote())
        .type(isPrimaryReference ? "amtlich" : "nichtamtlich")
        .legalPeriodicalRawValue(
            legalPeriodicalRawValue != null
                ? legalPeriodicalRawValue
                : reference.legalPeriodicalRawValue()) // fallback to raw value
        .documentationUnit(documentationUnitDTO)
        .build();
  }
}
