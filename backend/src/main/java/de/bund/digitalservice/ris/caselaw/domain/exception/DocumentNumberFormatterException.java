package de.bund.digitalservice.ris.caselaw.domain.exception;

import java.io.IOException;

public class DocumentNumberFormatterException extends IOException {
  public DocumentNumberFormatterException(String message) {
    super(message);
  }

  public DocumentNumberFormatterException(String message, Throwable cause) {
    super(message, cause);
  }
}
