package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;

public class DocumentTypeTransformer {
  private DocumentTypeTransformer() {}

  public static DocumentType transformToDomain(DocumentTypeDTO documentTypeDTO) {
    return DocumentType.builder()
        .jurisShortcut(documentTypeDTO.getAbbreviation())
        .label(documentTypeDTO.getLabel())
        .build();
  }
}
