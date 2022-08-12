package de.bund.digitalservice.ris.domain;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface XmlMailRepository extends ReactiveCrudRepository<XmlMail, Long> {

  Mono<XmlMail> findTopByDocumentUnitIdOrderByPublishDateDesc(Long documentUnitId);
}
