package de.bund.digitalservice.ris.domain;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface EmailPublishService {
  Mono<MailResponse> publish(DocUnit documentUnit, String receiverAddress);

  Mono<MailResponse> getLastPublishedXml(Long documentUnitId, UUID documentUnitUuid);
}
