package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileNumberRepository extends R2dbcRepository<FileNumberDTO, Long> {

  Mono<Void> deleteAllByDocumentUnitId(Long documentUnitId);

  Flux<FileNumberDTO> findAllByDocumentUnitId(Long documentUnitId);

  Mono<FileNumberDTO> findFirstByDocumentUnitIdAndIsDeviating(
      Long documentUnitId, Boolean isDeviating);

  Flux<FileNumberDTO> findAllByDocumentUnitIdAndIsDeviating(
      Long documentUnitId, boolean isDeviating);
}
