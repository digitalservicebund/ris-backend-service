package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DatabaseDocumentUnitFieldsOfLawRepository
    extends R2dbcRepository<DocumentUnitFieldsOfLawDTO, Long> {
  Flux<DocumentUnitFieldsOfLawDTO> findAllByDocumentUnitId(Long id);

  Mono<DocumentUnitFieldsOfLawDTO> findByDocumentUnitIdAndFieldOfLawId(
      Long documentUnitId, Long fieldOfLawId);
}
