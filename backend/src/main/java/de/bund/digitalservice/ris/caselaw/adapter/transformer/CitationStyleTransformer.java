package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CitationStyleDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationStyle;

public class CitationStyleTransformer {
  private CitationStyleTransformer() {}

  public static CitationStyle transformToDomain(CitationStyleDTO citationStyleDTO) {
    return new CitationStyle(
        citationStyleDTO.getUuid(),
        citationStyleDTO.getDocumentType(),
        citationStyleDTO.getCitationDocumentType(),
        citationStyleDTO.getJurisShortcut(),
        citationStyleDTO.getLabel());
  }
}
