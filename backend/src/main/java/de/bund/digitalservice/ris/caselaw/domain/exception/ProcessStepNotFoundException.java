package de.bund.digitalservice.ris.caselaw.domain.exception;

public class ProcessStepNotFoundException extends RuntimeException {
  public ProcessStepNotFoundException(String message) {
    super(message);
  }
}
