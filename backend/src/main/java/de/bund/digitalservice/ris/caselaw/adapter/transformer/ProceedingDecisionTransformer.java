package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitMetadataDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;

public class ProceedingDecisionTransformer {
  private ProceedingDecisionTransformer() {}

  public static ProceedingDecision transformToDomain(
      DocumentUnitMetadataDTO documentUnitMetadataDTO) {
    Court court = null;
    if (documentUnitMetadataDTO.getCourtType() != null) {
      court =
          Court.builder()
              .type(documentUnitMetadataDTO.getCourtType())
              .location(documentUnitMetadataDTO.getCourtLocation())
              .label(
                  Court.generateLabel(
                      documentUnitMetadataDTO.getCourtType(),
                      documentUnitMetadataDTO.getCourtLocation()))
              .build();
    }

    String fileNumber = null;
    if (documentUnitMetadataDTO.getFileNumbers() != null
        && !documentUnitMetadataDTO.getFileNumbers().isEmpty()) {
      fileNumber = documentUnitMetadataDTO.getFileNumbers().get(0).getFileNumber();
    }

    return ProceedingDecision.builder()
        .uuid(documentUnitMetadataDTO.getUuid())
        .documentNumber(documentUnitMetadataDTO.getDocumentnumber())
        .dataSource(documentUnitMetadataDTO.getDataSource())
        .court(court)
        .fileNumber(fileNumber)
        .documentType(getDocumentTypeByDTO(documentUnitMetadataDTO.getDocumentTypeDTO()))
        .date(documentUnitMetadataDTO.getDecisionDate())
        .build();
  }

  private static DocumentType getDocumentTypeByDTO(DocumentTypeDTO documentTypeDTO) {
    if (documentTypeDTO == null
        || (documentTypeDTO.getLabel() == null && documentTypeDTO.getJurisShortcut() == null)) {
      return null;
    }
    return DocumentType.builder()
        .label(documentTypeDTO.getLabel())
        .jurisShortcut(documentTypeDTO.getJurisShortcut())
        .build();
  }
}
