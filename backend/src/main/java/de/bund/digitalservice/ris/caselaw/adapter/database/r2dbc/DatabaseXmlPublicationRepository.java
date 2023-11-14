package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DatabaseXmlPublicationRepository extends R2dbcRepository<XmlPublicationDTO, Long> {

  Mono<XmlPublicationDTO> findTopByDocumentUnitIdOrderByPublishDateDesc(Long documentUnitId);

  Flux<XmlPublicationDTO> findAllByDocumentUnitIdOrderByPublishDateDesc(Long documentUnitId);
}
