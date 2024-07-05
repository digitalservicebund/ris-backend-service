package de.bund.digitalservice.ris.caselaw.domain.exception;

public class ImportApiKeyException extends RuntimeException {
  public ImportApiKeyException() {
    super();
  }

  public ImportApiKeyException(String message) {
    super(message);
  }
}
