package de.bund.digitalservice.ris.caselaw.domain;

/** Exception thrown when a documentation unit handover fails. */
public class DocumentationUnitHandoverException extends RuntimeException {
  public DocumentationUnitHandoverException(String message) {
    super(message);
  }

  public DocumentationUnitHandoverException(String message, Throwable cause) {
    super(message, cause);
  }
}
