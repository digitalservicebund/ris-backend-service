package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import reactor.core.publisher.Mono;

/** Interface for publish a document unit via email. */
public interface EmailPublishService {
  Mono<MailResponse> publish(DocumentUnitDTO documentUnitDTO, String receiverAddress);

  Mono<MailResponse> getLastPublishedXml(Long documentUnitId, UUID documentUnitUuid);
}
