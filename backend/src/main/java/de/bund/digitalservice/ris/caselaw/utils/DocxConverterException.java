package de.bund.digitalservice.ris.caselaw.utils;

public class DocxConverterException extends RuntimeException {
  public DocxConverterException(String message, Exception exception) {
    super(message, exception);
  }

  public DocxConverterException(String message) {
    super(message);
  }
}
