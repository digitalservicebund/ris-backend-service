package de.bund.digitalservice.ris.caselaw.adapter.exception;

/** Documentation unit could not be published to or withdrawn from portal */
public class PublishException extends RuntimeException {
  public PublishException(String message, Exception exception) {
    super(message, exception);
  }
}
