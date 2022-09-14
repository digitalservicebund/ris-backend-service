package de.bund.digitalservice.ris.domain;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DocUnitRepository extends ReactiveSortingRepository<DocUnitDTO, Long> {

  @Query("SELECT * FROM doc_unit WHERE documentnumber = $1")
  Mono<DocUnitDTO> findByDocumentnumber(String documentnumber);

  @Query("SELECT * FROM doc_unit WHERE uuid = $1")
  Mono<DocUnitDTO> findByUuid(UUID uuid);
}
