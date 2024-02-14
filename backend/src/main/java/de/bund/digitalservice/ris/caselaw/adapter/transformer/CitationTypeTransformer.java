package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CitationTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CitationTypeTransformer {
  private CitationTypeTransformer() {}

  public static CitationType transformToDomain(CitationTypeDTO citationTypeDTO) {
    if (citationTypeDTO == null) {
      return null;
    }

    log.debug("transform '{}' to citation style domain object", citationTypeDTO.getId());

    return CitationType.builder()
        .uuid(citationTypeDTO.getId())
        .label(citationTypeDTO.getLabel())
        .jurisShortcut(citationTypeDTO.getAbbreviation())
        .build();
  }
}
