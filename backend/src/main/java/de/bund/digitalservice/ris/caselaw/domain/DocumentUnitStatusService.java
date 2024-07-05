package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;

public interface DocumentUnitStatusService {

  void update(String documentNumber, Status status) throws DocumentationUnitNotExistsException;

  void update(UUID documentUuid, Status status) throws DocumentationUnitNotExistsException;

  PublicationStatus getLatestStatus(UUID documentUuid);

  PublicationStatus getLatestStatus(String documentNumber);
}
