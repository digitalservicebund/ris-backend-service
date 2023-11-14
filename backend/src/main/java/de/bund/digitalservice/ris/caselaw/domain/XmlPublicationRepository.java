package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface XmlPublicationRepository {
  Mono<XmlPublication> save(XmlPublication xmlPublication);

  Flux<Publication> getPublicationsByDocumentUnitUuid(UUID documentUnitUuid);

  Mono<XmlPublication> getLastXmlPublication(UUID documentUnitUuid);
}
