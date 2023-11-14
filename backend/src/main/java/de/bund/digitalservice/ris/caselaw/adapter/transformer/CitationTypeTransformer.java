package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CitationTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CitationTypeTransformer {
  private CitationTypeTransformer() {}

  public static CitationType transformToDomain(CitationTypeDTO citationTypeDTO) {
    if (log.isDebugEnabled()) {
      log.debug("transform '{}' to citation style domain object", citationTypeDTO.getId());
    }

    if (citationTypeDTO == null) {
      return null;
    }

    return CitationType.builder()
        .uuid(citationTypeDTO.getId())
        .citationDocumentType(citationTypeDTO.getCitationDocumentCategory().getLabel())
        .documentType(citationTypeDTO.getDocumentationUnitDocumentCategory().getLabel())
        .label(citationTypeDTO.getLabel())
        .jurisShortcut(citationTypeDTO.getAbbreviation())
        .build();
  }
}
