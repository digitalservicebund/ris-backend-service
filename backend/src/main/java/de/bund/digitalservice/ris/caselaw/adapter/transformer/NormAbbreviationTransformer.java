package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.time.ZoneId;
import java.util.List;

/**
 * Utility class for transforming NormAbbreviation objects between DTOs (Data Transfer Objects) and
 * domain objects.
 */
public class NormAbbreviationTransformer {

  private NormAbbreviationTransformer() {
    // Private constructor to prevent instantiation of this utility class.
  }

  /**
   * Transforms a NormAbbreviationDTO (Data Transfer Object) into a NormAbbreviation domain object.
   *
   * @param normAbbreviationDTO The NormAbbreviationDTO to be transformed.
   * @return The NormAbbreviation domain object representing the transformed NormAbbreviationDTO.
   */
  public static NormAbbreviation transformToDomain(NormAbbreviationDTO normAbbreviationDTO) {
    if (normAbbreviationDTO == null) {
      return null;
    }

    List<DocumentType> documentTypes = null;
    if (normAbbreviationDTO.getDocumentTypeList() != null) {
      documentTypes =
          normAbbreviationDTO.getDocumentTypeList().stream()
              .map(DocumentTypeTransformer::transformToDomain)
              .toList();
    }
    Region region = null;
    if (normAbbreviationDTO.getRegion() != null) {
      region = RegionTransformer.transformDTO(normAbbreviationDTO.getRegion());
    }

    return NormAbbreviation.builder()
        .id(normAbbreviationDTO.getId())
        .abbreviation(normAbbreviationDTO.getAbbreviation())
        .decisionDate(
            normAbbreviationDTO.getDecisionDate() != null
                ? normAbbreviationDTO
                    .getDecisionDate()
                    .atStartOfDay()
                    .atZone(ZoneId.of("Europe/Berlin"))
                    .toInstant()
                : null)
        .documentId(normAbbreviationDTO.getDocumentId())
        .documentNumber(normAbbreviationDTO.getDocumentNumber())
        .officialLetterAbbreviation(normAbbreviationDTO.getOfficialLetterAbbreviation())
        .officialLongTitle(normAbbreviationDTO.getOfficialLongTitle())
        .officialShortTitle(normAbbreviationDTO.getOfficialShortTitle())
        .source(normAbbreviationDTO.getSource())
        .documentTypes(documentTypes)
        .region(region)
        .build();
  }
}
