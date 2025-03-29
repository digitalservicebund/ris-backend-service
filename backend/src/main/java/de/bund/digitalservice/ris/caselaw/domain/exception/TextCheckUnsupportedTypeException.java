package de.bund.digitalservice.ris.caselaw.domain.exception;

public class TextCheckUnsupportedTypeException extends RuntimeException {

  public TextCheckUnsupportedTypeException() {
    super("Selected type is not supported");
  }

  public TextCheckUnsupportedTypeException(String message) {
    super(message);
  }

  public TextCheckUnsupportedTypeException(String message, Exception ex) {
    super(message, ex);
  }
}
