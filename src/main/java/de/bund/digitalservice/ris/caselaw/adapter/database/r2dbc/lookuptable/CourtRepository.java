package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CourtRepository extends ReactiveSortingRepository<CourtDTO, Long> {

  Mono<CourtDTO> findByCourttypeAndCourtlocation(String courttype, String courtlocation);

  Flux<CourtDTO> findAllByOrderByCourttypeAscCourtlocationAsc();

  @Query(
      "SELECT * FROM lookuptable_court WHERE UPPER(CONCAT(courttype, ' ', courtlocation)) LIKE UPPER('%'||:searchStr||'%') ORDER BY courttype, courtlocation")
  Flux<CourtDTO> findBySearchStr(String searchStr);
}
