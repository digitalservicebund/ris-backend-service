package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/** Spring implementation for the database request of the document units. */
@Repository
public interface DatabaseDocumentUnitRepository
    extends ReactiveSortingRepository<DocumentUnitDTO, Long> {

  Mono<DocumentUnitDTO> findByDocumentnumber(String documentnumber);

  Mono<DocumentUnitDTO> findByUuid(UUID uuid);
}
