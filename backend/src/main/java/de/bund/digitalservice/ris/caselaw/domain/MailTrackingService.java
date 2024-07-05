package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.http.ResponseEntity;

public interface MailTrackingService {

  EmailPublishState mapEventToPublishState(String mailTrackingEvent);

  ResponseEntity<String> processMailSendingState(String documentUnitUuid, String event);
}
