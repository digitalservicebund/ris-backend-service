package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormAbbreviationDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.DocumentTypeNew;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.NormAbbreviation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import java.time.ZoneId;
import java.util.List;

public class NormAbbreviationTransformer {
  private NormAbbreviationTransformer() {}

  public static NormAbbreviation transformDTO(NormAbbreviationDTO normAbbreviationDTO) {
    List<DocumentTypeNew> documentTypes = null;
    if (normAbbreviationDTO.getDocumentTypes() != null) {
      documentTypes =
          normAbbreviationDTO.getDocumentTypes().stream()
              .map(DocumentTypeNewTransformer::transformDTO)
              .toList();
    }
    List<Region> regions = null;
    if (normAbbreviationDTO.getRegions() != null) {
      regions =
          normAbbreviationDTO.getRegions().stream().map(RegionTransformer::transformDTO).toList();
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
        .regions(regions)
        .build();
  }
}
