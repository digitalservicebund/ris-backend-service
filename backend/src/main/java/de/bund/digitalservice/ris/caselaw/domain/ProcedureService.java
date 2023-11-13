package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProcedureService {
  Page<Procedure> search(
      Optional<String> query, DocumentationOffice documentationOffice, Pageable pageable);

  List<DocumentUnit> getDocumentUnits(UUID procedureid);

  void delete(UUID procedureId);
}
