package de.bund.digitalservice.ris.caselaw.domain;

public interface DocumentUnitStatusService {

  void update(String documentNumber, Status status) throws DocumentationUnitNotExistsException;

  PublicationStatus getLatestStatus(String documentNumber);
}
