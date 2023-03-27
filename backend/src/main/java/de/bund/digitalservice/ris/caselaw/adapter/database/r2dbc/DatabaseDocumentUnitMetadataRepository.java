package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DatabaseDocumentUnitMetadataRepository
    extends R2dbcRepository<DocumentUnitMetadataDTO, Long> {

  Mono<DocumentUnitMetadataDTO> findByUuid(UUID documentUnitUuid);

  Flux<DocumentUnitMetadataDTO> findAllByDataSourceLike(Sort sort, String dataSource);
}
