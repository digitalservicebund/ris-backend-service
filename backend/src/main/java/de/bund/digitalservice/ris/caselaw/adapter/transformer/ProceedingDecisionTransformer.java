package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision.ProceedingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;

public class ProceedingDecisionTransformer {
  private ProceedingDecisionTransformer() {}

  public static ProceedingDecision transformToDomain(ProceedingDecisionDTO proceedingDecisionDTO) {
    Court court = null;
    if (proceedingDecisionDTO.getCourtType() != null
        && proceedingDecisionDTO.getCourtLocation() != null) {
      court =
          new Court(
              proceedingDecisionDTO.getCourtType(),
              proceedingDecisionDTO.getCourtLocation(),
              proceedingDecisionDTO.getCourtType() + " " + proceedingDecisionDTO.getCourtLocation(),
              "");
    }

    String fileNumber = null;
    if (proceedingDecisionDTO.getFileNumbers() != null
        && !proceedingDecisionDTO.getFileNumbers().isEmpty()) {
      fileNumber = proceedingDecisionDTO.getFileNumbers().get(0).getFileNumber();
    }

    return ProceedingDecision.builder()
        .court(court)
        .uuid(proceedingDecisionDTO.getUuid())
        .fileNumber(fileNumber)
        .documentType(getDocumentTypeByDTO(proceedingDecisionDTO.getDocumentTypeDTO()))
        .date(proceedingDecisionDTO.getDecisionDate())
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
