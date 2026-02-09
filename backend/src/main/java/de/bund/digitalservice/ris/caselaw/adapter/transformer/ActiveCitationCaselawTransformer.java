package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationCaselawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CitationTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

@Slf4j
public class ActiveCitationCaselawTransformer extends RelatedDocumentationUnitTransformer {
  public static ActiveCitation transformToDomain(
      ActiveCitationCaselawDTO activeCitationCaselawDTO) {

    CitationTypeDTO citationTypeDTO = activeCitationCaselawDTO.getCitationType();
    CitationType citationType = null;
    if (citationTypeDTO != null && citationTypeDTO.getId() != null) {
      citationType =
          CitationType.builder()
              .uuid(citationTypeDTO.getId())
              .jurisShortcut(citationTypeDTO.getAbbreviation())
              .label(citationTypeDTO.getLabel())
              .build();
    }

    return ActiveCitation.builder()
        .uuid(activeCitationCaselawDTO.getId())
        .documentNumber(activeCitationCaselawDTO.getTargetDocumentNumber())
        .court(getCourtFromDTO(activeCitationCaselawDTO.getTargetCourt()))
        .fileNumber(activeCitationCaselawDTO.getTargetFileNumber())
        .documentType(getDocumentTypeFromDTO(activeCitationCaselawDTO.getTargetDocumentType()))
        .decisionDate(activeCitationCaselawDTO.getTargetDate())
        .citationType(citationType)
        .build();
  }

  public static @NonNull ActiveCitationCaselawDTO transformToDTO(
      ActiveCitation activeCitation, int rank) {
    ActiveCitationCaselawDTO.ActiveCitationCaselawDTOBuilder activeCitationCaselawDTOBuilder =
        ActiveCitationCaselawDTO.builder()
            .id(activeCitation.getUuid())
            .targetCourt(getCourtFromDomain(activeCitation.getCourt()))
            .targetDate(activeCitation.getDecisionDate())
            .targetDocumentNumber(activeCitation.getDocumentNumber())
            .targetDocumentType(getDocumentTypeFromDomain(activeCitation.getDocumentType()))
            .targetFileNumber(StringUtils.normalizeSpace(activeCitation.getFileNumber()))
            .rank(rank);

    CitationType citationType = activeCitation.getCitationType();

    if (citationType != null && citationType.uuid() != null) {
      CitationTypeDTO.CitationTypeDTOBuilder citationTypeDTOBuilder =
          CitationTypeDTO.builder().id(citationType.uuid());

      activeCitationCaselawDTOBuilder.citationType(citationTypeDTOBuilder.build());
    }

    return activeCitationCaselawDTOBuilder.build();
  }
}
