package de.bund.digitalservice.ris.domain;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface DocumentUnitPublishService {
  Mono<? extends ExportObject> publish(DocUnit documentUnit);

  Mono<? extends ExportObject> getLastPublishedXml(Long documentUnitId, UUID documentUnitUuid);
}
