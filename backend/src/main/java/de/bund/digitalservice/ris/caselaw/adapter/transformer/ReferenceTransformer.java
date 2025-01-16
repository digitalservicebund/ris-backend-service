package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LiteratureReferenceDTO;
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

    Reference.ReferenceBuilder builder =
        Reference.builder()
            .id(referenceDTO.getId())
            .primaryReference(legalPeriodical != null ? legalPeriodical.primaryReference() : null)
            .citation(referenceDTO.getCitation())
            .documentationUnit(
                RelatedDocumentationUnitTransformer.transformToDomain(
                    referenceDTO.getDocumentationUnit()))
            .documentationUnitRank(referenceDTO.getDocumentationUnitRank())
            .editionRank(referenceDTO.getEditionRank())
            .legalPeriodical(legalPeriodical)
            .legalPeriodicalRawValue(
                legalPeriodical != null
                    ? legalPeriodical.abbreviation()
                    : referenceDTO.getLegalPeriodicalRawValue()); // fallback to raw value

    if (referenceDTO instanceof CaselawReferenceDTO caselawReferenceDTO) {
      Boolean primaryFromType =
          caselawReferenceDTO.getType() != null
              ? caselawReferenceDTO.getType().equals("amtlich") // fallback to raw value
              : null;
      Boolean isPrimaryReference =
          builder.build().primaryReference() != null
              ? builder.build().primaryReference()
              : primaryFromType;

      if (isPrimaryReference == null) {
        throw new IllegalArgumentException(
            "Either the referenceDTO's legalPeriodical or type field must be set");
      }

      return builder
          .primaryReference(isPrimaryReference)
          .referenceSupplement(caselawReferenceDTO.getReferenceSupplement())
          .footnote(caselawReferenceDTO.getFootnote())
          .referenceType(ReferenceType.CASELAW)
          .build();

    } else if (referenceDTO instanceof LiteratureReferenceDTO literatureReferenceDTO) {
      return builder
          .author(literatureReferenceDTO.getAuthor())
          .documentType(
              DocumentTypeTransformer.transformToDomain(literatureReferenceDTO.getDocumentType()))
          .referenceType(ReferenceType.LITERATURE)
          .build();

    } else {
      throw new IllegalArgumentException("Unsupported referenceDTO type");
    }
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

    if (reference.referenceType().equals(ReferenceType.CASELAW)) {
      Boolean isPrimaryReference =
          legalPeriodicalDTO != null
              ? legalPeriodicalDTO.getPrimaryReference()
              : reference.primaryReference(); // fallback to nichtamtlich

      if (isPrimaryReference == null) {
        throw new IllegalArgumentException(
            "Either the reference's legalPeriodical or primaryReference field must be set");
      }

      return CaselawReferenceDTO.builder()
          .id(reference.id())
          .legalPeriodical(legalPeriodicalDTO)
          .citation(reference.citation())
          .legalPeriodicalRawValue(
              legalPeriodicalRawValue != null
                  ? legalPeriodicalRawValue
                  : reference.legalPeriodicalRawValue()) // fallback to raw value
          .documentationUnit(documentationUnitDTO)
          .referenceSupplement(reference.referenceSupplement())
          .footnote(reference.footnote())
          .type(isPrimaryReference ? "amtlich" : "nichtamtlich")
          .build();
    } else if (reference.referenceType().equals(ReferenceType.LITERATURE)) {
      return LiteratureReferenceDTO.builder()
          .id(reference.id())
          .legalPeriodical(legalPeriodicalDTO)
          .citation(reference.citation())
          .legalPeriodicalRawValue(
              legalPeriodicalRawValue != null
                  ? legalPeriodicalRawValue
                  : reference.legalPeriodicalRawValue()) // fallback to raw value
          .documentationUnit(documentationUnitDTO)
          .author(reference.author())
          .documentType(DocumentTypeTransformer.transformToDTO(reference.documentType()))
          .build();
    } else {
      throw new IllegalArgumentException("Unsupported reference type");
    }
  }
}
