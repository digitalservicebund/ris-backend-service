package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAKeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPANormDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPASubjectFieldDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.SubjectFieldDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision.ProceedingDecisionDTO;
import de.bund.digitalservice.ris.caselaw.domain.ProceedingDecision;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.subjectfield.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProceedingDecisionTransformer {
  private ProceedingDecisionTransformer() {}

  public static ProceedingDecision transformToDomain(ProceedingDecisionDTO proceedingDecisionDTO) {
    Court court = new Court(proceedingDecisionDTO.courtType(), proceedingDecisionDTO.courtLocation(), proceedingDecisionDTO.courtType() + " " + proceedingDecisionDTO.courtLocation(), "");
            return ProceedingDecision.builder()
                    .court(court)
                    .uuid(proceedingDecisionDTO.uuid())
                    .fileNumber(proceedingDecisionDTO.fileNumber())
                    .documentType(getDocumentTypeByDTO(proceedingDecisionDTO.documentTypeDTO()))
                    .date(proceedingDecisionDTO.decisionDate())
                    .build();
  }

  private static DocumentType getDocumentTypeByDTO(DocumentTypeDTO documentTypeDTO) {
    return DocumentType.builder()
            .label(documentTypeDTO.getLabel())
            .jurisShortcut(documentTypeDTO.getJurisShortcut())
            .build();
  }

}
