package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import java.util.List;

public interface DocumentationOfficeRepository {
  List<DocumentationOffice> findAll();
}
