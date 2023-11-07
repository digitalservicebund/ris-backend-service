package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CitationTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ActiveCitationTransformer extends RelatedDocumentationUnitTransformer {
  public static ActiveCitation transformToDomain(ActiveCitationDTO activeCitationDTO) {

    CitationTypeDTO citationTypeDTO = activeCitationDTO.getCitationType();
    CitationType citationType = null;
    if (citationTypeDTO != null) {
      citationType =
          CitationType.builder()
              .uuid(citationTypeDTO.getId())
              .documentType(citationTypeDTO.getDocumentationUnitDocumentCategory().getLabel())
              .citationDocumentType(citationTypeDTO.getCitationDocumentCategory().getLabel())
              .jurisShortcut(citationTypeDTO.getAbbreviation())
              .label(citationTypeDTO.getLabel())
              .build();
    }

    return ActiveCitation.builder()
        .uuid(activeCitationDTO.getId())
        .documentNumber(activeCitationDTO.getDocumentNumber())
        .court(getCourtFromDTO(activeCitationDTO.getCourt()))
        .fileNumber(getFileNumber(activeCitationDTO.getFileNumber()))
        .documentType(getDocumentTypeFromDTO(activeCitationDTO.getDocumentType()))
        .decisionDate(activeCitationDTO.getDate())
        .citationType(citationType)
        .build();
  }

  public static ActiveCitationDTO transformToDTO(ActiveCitation activeCitation, Integer rank) {
    if (activeCitation.hasNoValues()) {
      return null;
    }

    ActiveCitationDTO.ActiveCitationDTOBuilder activeCitationDTOBuilder =
        ActiveCitationDTO.builder()
            .id(activeCitation.getUuid())
            .court(getCourtFromDomain(activeCitation.getCourt()))
            .date(activeCitation.getDecisionDate())
            .documentNumber(activeCitation.getDocumentNumber())
            .documentType(getDocumentTypeFromDomain(activeCitation.getDocumentType()))
            .fileNumber(getFileNumber(activeCitation.getFileNumber()))
            .rank(rank);

    CitationType citationType = activeCitation.getCitationType();

    if (citationType != null) {
      CitationTypeDTO.CitationTypeDTOBuilder citationTypeDTOBuilder =
          CitationTypeDTO.builder().id(citationType.uuid());

      activeCitationDTOBuilder.citationType(citationTypeDTOBuilder.build());
    }

    return activeCitationDTOBuilder.build();
  }
}
