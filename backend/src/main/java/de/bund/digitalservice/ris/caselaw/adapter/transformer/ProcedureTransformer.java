package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureDTO;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProcedureTransformer {
  public static Procedure transformToDomain(List<ProcedureDTO> procedureDTOs) {
    if (procedureDTOs == null || procedureDTOs.isEmpty()) return null;

    ProcedureDTO currentProcedureDTO = procedureDTOs.get(0);
    return Procedure.builder()
        .label(currentProcedureDTO.getLabel())
        .createdAt(currentProcedureDTO.getCreatedAt())
        .documentUnitCount(getDocumentationUnitCount(currentProcedureDTO))
        .build();
  }

  private static Integer getDocumentationUnitCount(ProcedureDTO procedureDTO) {
    return Optional.ofNullable(procedureDTO.getDocumentationUnits()).map(List::size).orElse(null);
  }
}
