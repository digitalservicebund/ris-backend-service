package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LegalPeriodicalDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.domain.Reference;
import de.bund.digitalservice.ris.caselaw.domain.ReferenceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import lombok.experimental.UtilityClass;

@UtilityClass
public class PassiveCitationUliTransformer {

  public static Reference transformToDomain(PassiveCitationUliDTO dto) {
    if (dto == null) {
      return null;
    }

    LegalPeriodical legalPeriodical = null;
    if (dto.getSourceLegalPeriodical() != null) {
      legalPeriodical =
          LegalPeriodicalTransformer.transformToDomain(dto.getSourceLegalPeriodical());
    }

    return Reference.builder()
        .id(dto.getId())
        .author(dto.getSourceAuthor())
        .citation(dto.getSourceCitation())
        .legalPeriodical(legalPeriodical)
        .legalPeriodicalRawValue(
            legalPeriodical != null
                ? legalPeriodical.abbreviation()
                : dto.getSourceLegalPeriodicalRawValue())
        .documentType(DocumentTypeTransformer.transformToDomain(dto.getSourceDocumentType()))
        .referenceType(ReferenceType.LITERATURE)
        .build();
  }

  public static PassiveCitationUliDTO transformToDTO(Reference reference) {

    LegalPeriodicalDTO legalPeriodicalDTO = null;
    String legalPeriodicalRawValue = null;

    if (reference.legalPeriodical() != null) {
      legalPeriodicalDTO = LegalPeriodicalTransformer.transformToDTO(reference.legalPeriodical());
      legalPeriodicalRawValue = reference.legalPeriodical().abbreviation();
    }

    return PassiveCitationUliDTO.builder()
        .id(reference.id())
        .sourceAuthor(reference.author())
        .sourceCitation(reference.citation())
        .sourceLegalPeriodical(legalPeriodicalDTO)
        .sourceLegalPeriodicalRawValue(
            legalPeriodicalRawValue != null
                ? legalPeriodicalRawValue
                : reference.legalPeriodicalRawValue())
        .sourceDocumentType(DocumentTypeTransformer.transformToDTO(reference.documentType()))
        .build();
  }
}
