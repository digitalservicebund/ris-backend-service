package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcedureDTO;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ProcedureService {
  Slice<Procedure> search(
      Optional<String> query,
      DocumentationOffice documentationOffice,
      Pageable pageable,
      Optional<Boolean> withDocUnits);

  List<DocumentationUnitListItem> getDocumentationUnits(UUID procedureid);

  ProcedureDTO getByUUID(UUID uuid);

  String assignUserGroup(UUID procedureUUID, UUID userGroupId);

  String unassignUserGroup(UUID procedureUUID);

  void delete(UUID procedureId);
}
