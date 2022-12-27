package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface DatabaseXmlMailRepository extends ReactiveCrudRepository<XmlMailDTO, Long> {

  Mono<XmlMailDTO> findTopByDocumentUnitIdOrderByPublishDateDesc(Long documentUnitId);
}
