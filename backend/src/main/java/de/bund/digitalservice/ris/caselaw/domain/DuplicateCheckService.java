package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;

public interface DuplicateCheckService {
  void checkDuplicates(String docNumber);

  void checkAllDuplicates();

  String updateDuplicateStatus(
      String docNumberOrigin, String docNumberDuplicate, DuplicateRelationStatus status)
      throws DocumentationUnitNotExistsException;
}
