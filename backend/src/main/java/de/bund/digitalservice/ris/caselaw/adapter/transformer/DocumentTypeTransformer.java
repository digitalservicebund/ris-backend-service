package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;

public class DocumentTypeTransformer {
  private DocumentTypeTransformer() {}

  public static DocumentType transformDTO(DocumentTypeDTO documentTypeDTO) {
    return DocumentType.builder()
        .jurisShortcut(documentTypeDTO.getJurisShortcut())
        .label(documentTypeDTO.getLabel())
        .build();
  }
}
