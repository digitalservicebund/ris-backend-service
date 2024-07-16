package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.http.ResponseEntity;

public interface MailTrackingService {

  EmailStatus mapEventToStatus(String mailTrackingEvent);

  ResponseEntity<String> processMailSendingState(String documentUnitUuid, String event);
}
