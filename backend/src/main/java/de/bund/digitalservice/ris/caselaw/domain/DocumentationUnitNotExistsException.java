package de.bund.digitalservice.ris.caselaw.domain;

public class DocumentationUnitNotExistsException extends RuntimeException {
  public DocumentationUnitNotExistsException(String message) {
    super(message);
  }
}
