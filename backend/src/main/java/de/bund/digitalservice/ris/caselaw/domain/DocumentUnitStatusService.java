package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;

public interface DocumentUnitStatusService {

  void update(String documentNumber, Status status) throws DocumentationUnitNotExistsException;

  PublicationStatus getLatestStatus(String documentNumber);
}
