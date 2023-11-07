package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface XmlPublicationRepository {
  XmlPublication save(XmlPublication xmlPublication);

  List<Publication> getPublicationsByDocumentUnitUuid(UUID documentUnitUuid);

  Mono<XmlPublication> getLastXmlPublication(UUID documentUnitUuid);
}
