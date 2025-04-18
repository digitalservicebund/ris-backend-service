package de.bund.digitalservice.ris.caselaw.domain.exception;

public class DocumentNumberPatternException extends RuntimeException {
  public DocumentNumberPatternException(String message) {
    super(message);
  }

  public DocumentNumberPatternException(String message, Throwable cause) {
    super(message, cause);
  }
}
