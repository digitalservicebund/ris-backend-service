package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.DocumentTypeNew;

public class DocumentTypeNewTransformer {
  private DocumentTypeNewTransformer() {}

  public static DocumentTypeNew transformDTO(DocumentTypeDTO documentTypeNewDTO) {
    return DocumentTypeNew.builder()
        .abbreviation(documentTypeNewDTO.getAbbreviation())
        .label(documentTypeNewDTO.getLabel())
        .multiple(documentTypeNewDTO.getMultiple())
        .superLabel1(documentTypeNewDTO.getSuperLabel1())
        .superLabel2(documentTypeNewDTO.getSuperLabel2())
        .categoryLabel(documentTypeNewDTO.getCategory().getLabel())
        .build();
  }
}
