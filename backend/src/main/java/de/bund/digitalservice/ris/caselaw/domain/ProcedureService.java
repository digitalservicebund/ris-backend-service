package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProcedureService {
  Page<Procedure> search(
      Optional<String> query, DocumentationOffice documentationOffice, Pageable pageable);

  List<DocumentationUnitSearchEntry> getDocumentUnits(
      String procedureLabel, DocumentationOffice documentationOffice);
}
