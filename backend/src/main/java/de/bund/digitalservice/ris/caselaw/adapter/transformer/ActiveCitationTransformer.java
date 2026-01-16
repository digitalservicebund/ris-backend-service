package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveBlindlinkCaselawCitationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCaselawCitationDTO;
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

  public static ActiveCitation transformToDomain(ActiveCaselawCitationDTO activeCitationDTO) {
    return switch (activeCitationDTO) {
      case ActiveBlindlinkCaselawCitationDTO activeBlindlinkCaselawCitationDTO ->
          ActiveCitationTransformer.transformToDomain(activeBlindlinkCaselawCitationDTO);
      case LinkCaselawCitationDTO linkCaselawCitationDTO ->
          ActiveCitationTransformer.transformToDomain(linkCaselawCitationDTO);
    };
  }

  public static ActiveCitation transformToDomain(LinkCaselawCitationDTO linkCaselawCitationDTO) {
    CitationTypeDTO citationTypeDTO = linkCaselawCitationDTO.getCitationType();
    CitationType citationType = null;
    if (citationTypeDTO != null && citationTypeDTO.getId() != null) {
      citationType =
          CitationType.builder()
              .uuid(citationTypeDTO.getId())
              .jurisShortcut(citationTypeDTO.getAbbreviation())
              .label(citationTypeDTO.getLabel())
              .build();
    }

    String fileNumber = null;
    if (linkCaselawCitationDTO.getTargetDocument().getFileNumbers() != null
        && !linkCaselawCitationDTO.getTargetDocument().getFileNumbers().isEmpty()) {
      fileNumber =
          linkCaselawCitationDTO.getTargetDocument().getFileNumbers().getFirst().getValue();
    }

    return ActiveCitation.builder()
        .uuid(linkCaselawCitationDTO.getId())
        .documentNumber(linkCaselawCitationDTO.getTargetDocument().getDocumentNumber())
        .court(getCourtFromDTO(linkCaselawCitationDTO.getTargetDocument().getCourt()))
        .fileNumber(fileNumber)
        .documentType(
            getDocumentTypeFromDTO(linkCaselawCitationDTO.getTargetDocument().getDocumentType()))
        .decisionDate(linkCaselawCitationDTO.getTargetDocument().getDate())
        .citationType(citationType)
        .build();
  }

  public static ActiveCitation transformToDomain(
      ActiveBlindlinkCaselawCitationDTO activeBlindlinkCaselawCitationDTO) {
    CitationTypeDTO citationTypeDTO = activeBlindlinkCaselawCitationDTO.getCitationType();
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
        .uuid(activeBlindlinkCaselawCitationDTO.getId())
        .documentNumber(activeBlindlinkCaselawCitationDTO.getTargetDocumentNumber())
        .court(getCourtFromDTO(activeBlindlinkCaselawCitationDTO.getTargetCourt()))
        .fileNumber(activeBlindlinkCaselawCitationDTO.getTargetFileNumber())
        .documentType(
            getDocumentTypeFromDTO(activeBlindlinkCaselawCitationDTO.getTargetDocumentType()))
        .decisionDate(activeBlindlinkCaselawCitationDTO.getTargetDate())
        .citationType(citationType)
        .build();
  }
}
