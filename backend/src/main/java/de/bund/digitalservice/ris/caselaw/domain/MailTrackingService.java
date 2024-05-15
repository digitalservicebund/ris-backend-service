package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.http.ResponseEntity;

public interface MailTrackingService {

  EmailPublishState getMappedPublishState(String mailTrackingEvent);

  ResponseEntity<String> updatePublishingState(String documentUnitUuid, String event);
}
