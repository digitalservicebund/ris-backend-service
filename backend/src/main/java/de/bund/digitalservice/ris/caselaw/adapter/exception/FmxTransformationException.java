package de.bund.digitalservice.ris.caselaw.adapter.exception;

public class FmxTransformationException extends RuntimeException {
  public FmxTransformationException(String message, Exception ex) {
    super(message, ex);
  }

  public FmxTransformationException(String message) {
    super(message);
  }
}
