package de.bund.digitalservice.ris.caselaw.domain.exception;

public class ProcessStepMissingException extends RuntimeException {
  public ProcessStepMissingException(String message) {
    super(message);
  }
}
