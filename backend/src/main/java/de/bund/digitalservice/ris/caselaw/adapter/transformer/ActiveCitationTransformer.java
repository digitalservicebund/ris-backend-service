package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationDTO.ActiveCitationDTOBuilder;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CitationTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.domain.ActiveCitation;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

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

    Optional<DocumentationUnitDTO> referencedDocumentationUnit =
        Optional.ofNullable(activeCitationDTO.getReferencedDocumentationUnit());
    return ActiveCitation.builder()
        .uuid(activeCitationDTO.getId())
        .documentNumber(
            referencedDocumentationUnit.map(DocumentationUnitDTO::getDocumentNumber).orElse(null))
        .court(getCourtFromDTO(activeCitationDTO.getCourt()))
        .fileNumber(getFileNumber(activeCitationDTO.getFileNumber()))
        .documentType(getDocumentTypeFromDTO(activeCitationDTO.getDocumentType()))
        .decisionDate(activeCitationDTO.getDate())
        .citationType(citationType)
        .referenceFound(referencedDocumentationUnit.isPresent())
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
            .referencedDocumentationUnit(
                activeCitation.getDocumentNumber() == null
                    ? null
                    : DocumentationUnitDTO.builder()
                        .id(activeCitation.getUuid())
                        .documentNumber(activeCitation.getDocumentNumber())
                        .build())
            .documentType(getDocumentTypeFromDomain(activeCitation.getDocumentType()))
            .fileNumber(getFileNumber(activeCitation.getFileNumber()));

    CitationType citationType = activeCitation.getCitationType();

    if (citationType != null && citationType.uuid() != null) {
      CitationTypeDTO.CitationTypeDTOBuilder citationTypeDTOBuilder =
          CitationTypeDTO.builder().id(citationType.uuid());

      activeCitationDTOBuilder.citationType(citationTypeDTOBuilder.build());
    }

    return activeCitationDTOBuilder.build();
  }
}
