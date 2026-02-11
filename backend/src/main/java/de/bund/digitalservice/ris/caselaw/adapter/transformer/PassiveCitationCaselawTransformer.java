package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CitationTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationCaselawDTO;
import de.bund.digitalservice.ris.caselaw.domain.PassiveCaselawCitation;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

@Slf4j
public class PassiveCitationCaselawTransformer extends RelatedDocumentationUnitTransformer {
  public static PassiveCaselawCitation transformToDomain(
      PassiveCitationCaselawDTO passiveCitationCaselawDTO) {

    CitationTypeDTO citationTypeDTO = passiveCitationCaselawDTO.getCitationType();
    CitationType citationType = null;
    if (citationTypeDTO != null && citationTypeDTO.getId() != null) {
      citationType =
          CitationType.builder()
              .uuid(citationTypeDTO.getId())
              .jurisShortcut(citationTypeDTO.getAbbreviation())
              .label(citationTypeDTO.getLabel())
              .build();
    }

    return PassiveCaselawCitation.builder()
        .uuid(passiveCitationCaselawDTO.getId())
        .sourceDocumentNumber(passiveCitationCaselawDTO.getSourceDocumentNumber())
        .sourceCourt(getCourtFromDTO(passiveCitationCaselawDTO.getSourceCourt()))
        .sourceFileNumber(passiveCitationCaselawDTO.getSourceFileNumber())
        .sourceDocumentType(
            getDocumentTypeFromDTO(passiveCitationCaselawDTO.getSourceDocumentType()))
        .sourceDate(passiveCitationCaselawDTO.getSourceDate())
        .citationType(citationType)
        .build();
  }

  public static @NonNull PassiveCitationCaselawDTO transformToDTO(
      PassiveCaselawCitation passiveCaselawCitation, int rank) {
    PassiveCitationCaselawDTO.PassiveCitationCaselawDTOBuilder passiveCitationCaselawDTOBuilder =
        PassiveCitationCaselawDTO.builder()
            .id(passiveCaselawCitation.getUuid())
            .sourceCourt(getCourtFromDomain(passiveCaselawCitation.getSourceCourt()))
            .sourceDate(passiveCaselawCitation.getSourceDate())
            .sourceDocumentNumber(passiveCaselawCitation.getSourceDocumentNumber())
            .sourceDocumentType(
                getDocumentTypeFromDomain(passiveCaselawCitation.getSourceDocumentType()))
            .sourceFileNumber(
                StringUtils.normalizeSpace(passiveCaselawCitation.getSourceFileNumber()))
            .rank(rank);

    CitationType citationType = passiveCaselawCitation.getCitationType();

    if (citationType != null && citationType.uuid() != null) {
      CitationTypeDTO.CitationTypeDTOBuilder citationTypeDTOBuilder =
          CitationTypeDTO.builder().id(citationType.uuid());

      passiveCitationCaselawDTOBuilder.citationType(citationTypeDTOBuilder.build());
    }

    return passiveCitationCaselawDTOBuilder.build();
  }
}
