package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface MailTrackingService {

  EmailPublishState getMappedPublishState(String mailTrackingEvent);

  Mono<ResponseEntity<String>> updatePublishingState(String documentUnitUuid, String event);
}
