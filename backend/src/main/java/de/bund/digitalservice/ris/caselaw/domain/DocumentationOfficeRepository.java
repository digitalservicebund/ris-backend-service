package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationOfficeNotExistsException;
import java.util.List;
import java.util.UUID;

public interface DocumentationOfficeRepository {
  DocumentationOffice findByUuid(UUID uuid) throws DocumentationOfficeNotExistsException;

  List<DocumentationOffice> findBySearchStr(String searchStr);

  List<DocumentationOffice> findAllOrderByAbbreviationAsc();
}
