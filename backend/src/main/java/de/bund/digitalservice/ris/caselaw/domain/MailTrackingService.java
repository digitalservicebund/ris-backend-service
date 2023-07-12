package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface MailTrackingService {
  EmailPublishState getMappedPublishState(String mailTrackingEvent);

  Mono<UUID> setPublishState(UUID documentUnitUuid, EmailPublishState emailPublishState);
}
