package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ReferenceDTO;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ReferenceTransformer {
  public static Reference transformToDomain(ReferenceDTO dto) {
    if (dto == null) return null;

    LegalPeriodical periodical =
        dto.getLegalPeriodical() != null
            ? LegalPeriodicalTransformer.transformToDomain(dto.getLegalPeriodical())
            : null;

    Boolean periodicalReferenceType =
        (periodical != null && periodical.primaryReference() != null)
            ? periodical.primaryReference()
            : null;

    Boolean referenceType = dto.getType() != null ? dto.getType().equals("amtlich") : null;

    // Type kann unterschiedlich in LegalPeriodical und ReferenceDTO sein.
    Boolean isPrimary = (periodicalReferenceType != null) ? periodicalReferenceType : referenceType;

    if (isPrimary == null) {
      throw new IllegalArgumentException(
          "Type must be set in either legalPeriodical or referenceDTO itself");
    }

    return Reference.builder()
        .id(dto.getId())
        .citation(dto.getCitation())
        .documentationUnit(
            RelatedDocumentationUnitTransformer.transformToDomain(dto.getDocumentationUnit()))
        .legalPeriodical(periodical)
        .legalPeriodicalRawValue(
            periodical != null ? periodical.abbreviation() : dto.getLegalPeriodicalRawValue())
        .primaryReference(isPrimary)
        .referenceSupplement(dto.getReferenceSupplement())
        .footnote(dto.getFootnote())
        .referenceType(ReferenceType.CASELAW)
        .build();
  }

  public static ReferenceDTO transformToDTO(Reference reference) {
    LegalPeriodicalDTO legalPeriodicalDTO = null;
    String legalPeriodicalRawValue = null;

    if (reference.legalPeriodical() != null) {
      legalPeriodicalDTO = LegalPeriodicalTransformer.transformToDTO(reference.legalPeriodical());
      legalPeriodicalRawValue = reference.legalPeriodical().abbreviation();
    }

    Boolean isPrimaryReference =
        legalPeriodicalDTO != null
            ? legalPeriodicalDTO.getPrimaryReference()
            : reference.primaryReference(); // fallback to nichtamtlich

    if (isPrimaryReference == null) {
      throw new IllegalArgumentException(
          "Either the reference's legalPeriodical or primaryReference field must be set");
    }
    return ReferenceDTO.builder()
        .id(reference.id())
        .legalPeriodical(legalPeriodicalDTO)
        .citation(reference.citation())
        .legalPeriodicalRawValue(
            legalPeriodicalRawValue != null
                ? legalPeriodicalRawValue
                : reference.legalPeriodicalRawValue()) // fallback to raw value
        .referenceSupplement(reference.referenceSupplement())
        .footnote(reference.footnote())
        .type(isPrimaryReference ? "amtlich" : "nichtamtlich")
        .build();
  }
}
