package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureDTO;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProcedureTransformer {
  public static Procedure transformFirstToDomain(List<ProcedureDTO> procedureDTOs) {
    if (procedureDTOs == null || procedureDTOs.isEmpty()) return null;

    return transformToDomain(procedureDTOs.get(0));
  }

  public static List<String> transformPreviousProceduresToLabel(List<ProcedureDTO> procedureDTOs) {
    if (procedureDTOs == null || procedureDTOs.size() < 2) return null;

    return procedureDTOs.subList(1, procedureDTOs.size()).stream()
        .map(ProcedureDTO::getLabel)
        .toList();
  }

  private static Integer getDocumentationUnitCount(ProcedureDTO procedureDTO) {
    return Optional.ofNullable(procedureDTO.getDocumentationUnits()).map(List::size).orElse(null);
  }

  public static Procedure transformToDomain(ProcedureDTO procedureDTO) {
    if (procedureDTO == null) {
      return null;
    }

    return Procedure.builder()
        .label(procedureDTO.getLabel())
        .createdAt(procedureDTO.getCreatedAt())
        .documentUnitCount(getDocumentationUnitCount(procedureDTO))
        .build();
  }
}
