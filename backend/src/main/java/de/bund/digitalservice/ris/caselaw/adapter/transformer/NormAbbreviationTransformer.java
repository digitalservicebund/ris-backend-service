package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.DocumentTypeNew;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import java.time.ZoneId;
import java.util.List;

public class NormAbbreviationTransformer {
  private NormAbbreviationTransformer() {}

  public static NormAbbreviation transformDTO(NormAbbreviationDTO normAbbreviationDTO) {
    if (normAbbreviationDTO == null) {
      return null;
    }

    List<DocumentTypeNew> documentTypes = null;
    if (normAbbreviationDTO.getDocumentTypeList() != null) {
      documentTypes =
          normAbbreviationDTO.getDocumentTypeList().stream()
              .map(DocumentTypeNewTransformer::transformDTO)
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
