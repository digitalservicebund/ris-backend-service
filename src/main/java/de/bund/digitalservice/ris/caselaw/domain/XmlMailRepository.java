package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface XmlMailRepository extends R2dbcRepository<XmlMail, Long> {

  Mono<XmlMail> findTopByDocumentUnitIdOrderByPublishDateDesc(Long documentUnitId);
}
