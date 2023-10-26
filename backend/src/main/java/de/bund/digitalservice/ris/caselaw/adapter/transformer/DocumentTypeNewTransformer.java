package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeNewDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.DocumentTypeNew;

public class DocumentTypeNewTransformer {
  private DocumentTypeNewTransformer() {}

  public static DocumentTypeNew transformDTO(DocumentTypeNewDTO documentTypeNewDTO) {
    return DocumentTypeNew.builder()
        .abbreviation(documentTypeNewDTO.getAbbreviation())
        .label(documentTypeNewDTO.getLabel())
        .multiple(documentTypeNewDTO.isMultiple())
        .superLabel1(documentTypeNewDTO.getSuperLabel1())
        .superLabel2(documentTypeNewDTO.getSuperLabel2())
        .categoryLabel(documentTypeNewDTO.getCategoryLabel())
        .build();
  }
}
