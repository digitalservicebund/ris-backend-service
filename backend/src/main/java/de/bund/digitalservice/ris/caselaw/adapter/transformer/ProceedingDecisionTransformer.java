package de.bund.digitalservice.ris.caselaw.adapter.transformer;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision.ProceedingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;

public class ProceedingDecisionTransformer {
  private ProceedingDecisionTransformer() {}

  public static ProceedingDecision transformToDomain(ProceedingDecisionDTO proceedingDecisionDTO) {
    Court court = new Court(proceedingDecisionDTO.getCourtType(), proceedingDecisionDTO.getCourtLocation(), proceedingDecisionDTO.getCourtType() + " " + proceedingDecisionDTO.getCourtLocation(), "");
            return ProceedingDecision.builder()
                    .court(court)
                    .uuid(proceedingDecisionDTO.getUuid())
                    .fileNumber(proceedingDecisionDTO.getFileNumbers().get(0).getFileNumber())
                    .documentType(getDocumentTypeByDTO(proceedingDecisionDTO.getDocumentTypeDTO()))
                    .date(proceedingDecisionDTO.getDecisionDate())
                    .build();
  }

  private static DocumentType getDocumentTypeByDTO(DocumentTypeDTO documentTypeDTO) {
    return DocumentType.builder()
            .label(documentTypeDTO.getLabel())
            .jurisShortcut(documentTypeDTO.getJurisShortcut())
            .build();
  }

}
