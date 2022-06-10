package de.bund.digitalservice.ris.exception;

public class DocxConverterException extends RuntimeException {
  public DocxConverterException(String message, Exception exception) {
    super(message, exception);
  }
}
