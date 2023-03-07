package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitLink;

import java.util.List;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DatabaseLinkedDocumentUnitRepository
    extends R2dbcRepository<LinkedDocumentUnitDTO, Long> {
  Flux<LinkedDocumentUnitDTO> findById(Long Id);

  Flux<LinkedDocumentUnitDTO> saveAll(List<LinkedDocumentUnitDTO> previousDecisions);

  Mono<Void> deleteAllByDocumentUnitId(Long id);
}
