package de.bund.digitalservice.ris.domain;

public class DocxConverterException extends RuntimeException {
  public DocxConverterException(String message, Exception exception) {
    super(message, exception);
  }
}
