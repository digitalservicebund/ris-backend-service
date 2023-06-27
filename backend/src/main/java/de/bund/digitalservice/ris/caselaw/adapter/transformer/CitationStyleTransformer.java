package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CitationStyleDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationStyle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CitationStyleTransformer {
  private CitationStyleTransformer() {}

  public static CitationStyle transformToDomain(CitationStyleDTO citationStyleDTO) {
    if (log.isDebugEnabled()) {
      log.debug("transform '{}' to citation style domain object", citationStyleDTO.getUuid());
    }

    return new CitationStyle(
        citationStyleDTO.getUuid(),
        citationStyleDTO.getDocumentType(),
        citationStyleDTO.getCitationDocumentType(),
        citationStyleDTO.getJurisShortcut(),
        citationStyleDTO.getLabel());
  }
}
