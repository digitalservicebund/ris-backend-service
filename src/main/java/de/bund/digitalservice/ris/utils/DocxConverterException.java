package de.bund.digitalservice.ris.utils;

public class DocxConverterException extends RuntimeException {
  public DocxConverterException(String message, Exception exception) {
    super(message, exception);
  }

  public DocxConverterException(String message) {
    super(message);
  }
}
