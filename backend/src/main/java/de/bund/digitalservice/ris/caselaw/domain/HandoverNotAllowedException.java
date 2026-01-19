package de.bund.digitalservice.ris.caselaw.domain;

/** Exception thrown when a documentation unit handover is not allowed. */
public class HandoverNotAllowedException extends RuntimeException {
  public HandoverNotAllowedException(String message) {
    super(message);
  }
}
