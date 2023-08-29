package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;

public interface ProcedureService {
  List<Procedure> search(Optional<String> query, DocumentationOffice documentationOffice);

  void setInitialProcedure(DocumentUnit documentUnit);
}
