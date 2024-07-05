package de.bund.digitalservice.ris.caselaw.domain.exception;

public class DocumentUnitPublishException extends RuntimeException {
  public DocumentUnitPublishException(String message) {
    super(message);
  }

  public DocumentUnitPublishException(String message, Throwable cause) {
    super(message, cause);
  }
}
