package de.bund.digitalservice.ris.caselaw.domain;

public interface MailTrackingService {
  EmailPublishState getMappedPublishState(String mailTrackingEvent);
}
