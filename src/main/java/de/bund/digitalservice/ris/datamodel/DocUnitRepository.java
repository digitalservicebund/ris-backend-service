package de.bund.digitalservice.ris.datamodel;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
interface DocUnitRepository extends ReactiveCrudRepository<DocUnit, Integer> {

  @Query("select id, s3path, filetype from DOC_UNIT where filetype = $1")
  Flux<DocUnit> findByFileType(String filetype);
}
