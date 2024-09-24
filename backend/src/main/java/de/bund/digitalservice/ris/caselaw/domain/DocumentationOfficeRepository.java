package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;

public interface DocumentationOfficeRepository {
  List<DocumentationOffice> findAll();
}
