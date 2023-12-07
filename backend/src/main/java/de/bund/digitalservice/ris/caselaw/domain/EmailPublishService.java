package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmailPublishService {
  Mono<XmlPublication> publish(DocumentUnit documentUnit, String receiverAddress);

  Flux<Publication> getPublications(UUID documentUnitUuid);

  Mono<XmlResultObject> getPublicationPreview(DocumentUnit documentUnit);
}
