package de.bund.digitalservice.ris.domain;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface DocumentUnitPublishService {
  Mono<ExportObject> publish(DocUnit documentUnit);

  Mono<ExportObject> getLastPublishedXml(Long documentUnitId, UUID documentUnitUuid);
}
