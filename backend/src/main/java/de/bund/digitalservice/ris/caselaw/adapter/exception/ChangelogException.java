package de.bund.digitalservice.ris.caselaw.adapter.exception;

/** Changelog could not be created or uploaded. */
public class ChangelogException extends RuntimeException {
  public ChangelogException(String message, Exception exception) {
    super(message, exception);
  }
}
