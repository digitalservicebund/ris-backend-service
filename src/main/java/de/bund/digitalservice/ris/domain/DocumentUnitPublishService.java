package de.bund.digitalservice.ris.domain;

import reactor.core.publisher.Mono;

public interface DocumentUnitPublishService {
  Mono<? extends ExportObject> publish(DocUnit documentUnit);
}
