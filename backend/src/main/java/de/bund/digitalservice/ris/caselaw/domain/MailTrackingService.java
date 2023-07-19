package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

public interface MailTrackingService {

  EmailPublishState getMappedPublishState(String mailTrackingEvent);

  Mono<ResponseEntity<String>> updatePublishingState(UUID documentUnitUuid, String event);
}
