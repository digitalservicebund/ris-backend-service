package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureDTO;
import de.bund.digitalservice.ris.caselaw.domain.Procedure;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ProcedureTransformer {

  public static List<String> transformPreviousProceduresToLabel(List<ProcedureDTO> procedureDTOs) {
    if (procedureDTOs == null || procedureDTOs.size() < 2) {
      return Collections.emptyList();
    }

    // Skip the last procedure, as it is the current one
    return procedureDTOs.subList(0, procedureDTOs.size() - 1).stream()
        .map(ProcedureDTO::getLabel)
        .toList();
  }

  private static Long getDocumentationUnitCount(ProcedureDTO procedureDTO) {
    if (procedureDTO.getDocumentationUnits() == null
        || procedureDTO.getDocumentationUnits().isEmpty()) {
      return 0L;
    }

    return (long) procedureDTO.getDocumentationUnits().size();
  }

  public static Procedure transformToDomain(ProcedureDTO procedureDTO) {
    return transformToDomain(procedureDTO, true);
  }

  public static Procedure transformToDomain(ProcedureDTO procedureDTO, boolean withCount) {
    if (procedureDTO == null) {
      return null;
    }

    return Procedure.builder()
        .id(procedureDTO.getId())
        .label(procedureDTO.getLabel())
        .createdAt(procedureDTO.getCreatedAt())
        .documentationUnitCount(withCount ? getDocumentationUnitCount(procedureDTO) : 0)
        .userGroupId(extractUserGroupId(procedureDTO))
        .build();
  }

  private static UUID extractUserGroupId(ProcedureDTO procedureDTO) {
    if (procedureDTO.getUserGroupDTO() != null) {
      return procedureDTO.getUserGroupDTO().getId();
    }
    return null;
  }
}
