package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DatabaseDocumentUnitNormRepository
    extends R2dbcRepository<DocumentUnitNormDTO, Long> {
  Flux<DocumentUnitNormDTO> findAllByDocumentUnitId(Long documentUnitId);
}
