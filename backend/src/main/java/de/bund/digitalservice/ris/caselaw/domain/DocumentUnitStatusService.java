package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Instant;
import java.util.UUID;

public interface DocumentUnitStatusService {

  DocumentUnit setInitialStatus(DocumentUnit documentUnit)
      throws DocumentationUnitNotExistsException;

  DocumentUnit setToPublishing(DocumentUnit documentUnit, Instant publishDate, String issuerAddress)
      throws DocumentationUnitNotExistsException;

  void update(String documentNumber, Status status) throws DocumentationUnitNotExistsException;

  void update(UUID documentUuid, Status status) throws DocumentationUnitNotExistsException;

  String getLatestIssuerAddress(String documentNumber) throws DocumentationUnitNotExistsException;

  PublicationStatus getLatestStatus(UUID documentUuid);
}
