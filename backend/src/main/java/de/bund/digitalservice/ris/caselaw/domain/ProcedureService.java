package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ProcedureService {
  Slice<Procedure> search(
      Optional<String> query, DocumentationOffice documentationOffice, Pageable pageable);

  List<DocumentationUnitListItem> getDocumentUnits(UUID procedureid);

  void delete(UUID procedureId);
}
