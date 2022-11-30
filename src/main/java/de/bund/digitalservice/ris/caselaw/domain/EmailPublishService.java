package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface EmailPublishService {
  Mono<MailResponse> publish(DocumentUnit documentUnit, String receiverAddress);

  Mono<MailResponse> getLastPublishedXml(Long documentUnitId, UUID documentUnitUuid);
}
