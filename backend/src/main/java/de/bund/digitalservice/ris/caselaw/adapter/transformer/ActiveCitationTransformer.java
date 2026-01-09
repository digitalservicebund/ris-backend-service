package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationDTO.ActiveCitationDTOBuilder;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CaselawCitationBlindlinkDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CaselawCitationLinkDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CitationTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.StringUtils;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

@Slf4j
public class ActiveCitationTransformer extends RelatedDocumentationUnitTransformer {
  public static ActiveCitation transformToDomain(ActiveCitationDTO activeCitationDTO) {

    CitationTypeDTO citationTypeDTO = activeCitationDTO.getCitationType();
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
        .uuid(activeCitationDTO.getId())
        .documentNumber(activeCitationDTO.getDocumentNumber())
        .court(getCourtFromDTO(activeCitationDTO.getCourt()))
        .fileNumber(activeCitationDTO.getFileNumber())
        .documentType(getDocumentTypeFromDTO(activeCitationDTO.getDocumentType()))
        .decisionDate(activeCitationDTO.getDate())
        .citationType(citationType)
        .build();
  }

  public static ActiveCitationDTO transformToDTO(ActiveCitation activeCitation) {
    if (activeCitation.hasNoValues()) {
      return null;
    }

    ActiveCitationDTOBuilder<?, ?> activeCitationDTOBuilder =
        ActiveCitationDTO.builder()
            .id(activeCitation.getUuid())
            .court(getCourtFromDomain(activeCitation.getCourt()))
            .date(activeCitation.getDecisionDate())
            .documentNumber(activeCitation.getDocumentNumber())
            .documentType(getDocumentTypeFromDomain(activeCitation.getDocumentType()))
            .fileNumber(StringUtils.normalizeSpace(activeCitation.getFileNumber()));

    CitationType citationType = activeCitation.getCitationType();

    if (citationType != null && citationType.uuid() != null) {
      CitationTypeDTO.CitationTypeDTOBuilder citationTypeDTOBuilder =
          CitationTypeDTO.builder().id(citationType.uuid());

      activeCitationDTOBuilder.citationType(citationTypeDTOBuilder.build());
    }

    return activeCitationDTOBuilder.build();
  }

  public static CaselawCitationLinkDTO transformToCaselawCitationLinkDTO(
      ActiveCitation activeCitation,
      @NonNull DecisionDTO sourceDecisionDto,
      @NonNull DecisionDTO targetDecisionDto) {
    if (activeCitation.hasNoValues()) {
      return null;
    }

    return CaselawCitationLinkDTO.builder()
        .id(activeCitation.getUuid())
        .sourceDocument(sourceDecisionDto)
        .targetDocument(targetDecisionDto)
        .citationType(CitationTypeDTO.builder().id(activeCitation.getCitationType().uuid()).build())
        .build();
  }

  public static CaselawCitationBlindlinkDTO transformToCaselawCitationBlindlinkDTO(
      ActiveCitation activeCitation, DecisionDTO currentDto) {
    if (activeCitation.hasNoValues()) {
      return null;
    }

    return CaselawCitationBlindlinkDTO.builder()
        .id(activeCitation.getUuid())
        .sourceDocument(currentDto)
        .targetCourt(getCourtFromDomain(activeCitation.getCourt()))
        .targetDate(activeCitation.getDecisionDate())
        .targetDocumentNumber(activeCitation.getDocumentNumber())
        .targetDocumentType(getDocumentTypeFromDomain(activeCitation.getDocumentType()))
        .targetFileNumber(StringUtils.normalizeSpace(activeCitation.getFileNumber()))
        .citationType(CitationTypeDTO.builder().id(activeCitation.getCitationType().uuid()).build())
        .build();
  }
}
