package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;

public interface DocumentationUnitStatusService {

  void update(String documentNumber, Status status, User user)
      throws DocumentationUnitNotExistsException;

  PublicationStatus getLatestStatus(String documentNumber)
      throws DocumentationUnitNotExistsException;
}
