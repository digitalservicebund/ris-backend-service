package de.bund.digitalservice.ris.caselaw.adapter.exception;

public class FmxImporterException extends RuntimeException {
  public FmxImporterException(String message, Exception ex) {
    super(message, ex);
  }

  public FmxImporterException(String message) {
    super(message);
  }
}
