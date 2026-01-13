package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveBlindlinkCaselawCitationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationDTO.ActiveCitationDTOBuilder;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CitationTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.LinkCaselawCitationDTO;
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

  public static @NonNull LinkCaselawCitationDTO transformToCaselawCitationLinkDTO(
      @NonNull ActiveCitation activeCitation,
      @NonNull DecisionDTO sourceDecisionDto,
      @NonNull DecisionDTO targetDecisionDto,
      @NonNull Integer rank) {

    var builder =
        LinkCaselawCitationDTO.builder()
            .id(activeCitation.getUuid())
            .sourceDocument(sourceDecisionDto)
            .targetDocument(targetDecisionDto)
            .rank(rank);

    CitationType citationType = activeCitation.getCitationType();

    if (citationType != null && citationType.uuid() != null) {
      CitationTypeDTO.CitationTypeDTOBuilder citationTypeDTOBuilder =
          CitationTypeDTO.builder().id(citationType.uuid());

      builder.citationType(citationTypeDTOBuilder.build());
    }

    return builder.build();
  }

  public static @NonNull ActiveBlindlinkCaselawCitationDTO transformToCaselawCitationBlindlinkDTO(
      @NonNull ActiveCitation activeCitation,
      @NonNull DecisionDTO currentDto,
      @NonNull Integer rank) {
    var builder =
        ActiveBlindlinkCaselawCitationDTO.builder()
            .id(activeCitation.getUuid())
            .sourceDocument(currentDto)
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

      builder.citationType(citationTypeDTOBuilder.build());
    }

    return builder.build();
  }
}
