package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface DatabaseXmlMailRepository extends R2dbcRepository<XmlMailDTO, Long> {

  Mono<XmlMailDTO> findTopByDocumentUnitIdOrderByPublishDateDesc(Long documentUnitId);
}
