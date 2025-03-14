package de.bund.digitalservice.ris.caselaw.domain.exception;

public class TextCheckUnknownCategoryException extends RuntimeException {

  public TextCheckUnknownCategoryException() {
    super("Unknown category: Ensure that text category type is included");
  }

  public TextCheckUnknownCategoryException(String message) {
    super(message);
  }

  public TextCheckUnknownCategoryException(String message, Exception ex) {
    super(message, ex);
  }
}
