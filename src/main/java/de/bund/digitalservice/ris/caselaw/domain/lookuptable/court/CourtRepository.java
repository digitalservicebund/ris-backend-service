package de.bund.digitalservice.ris.caselaw.domain.lookuptable.court;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CourtRepository extends ReactiveSortingRepository<CourtDTO, Long> {

  @Query(
      "SELECT * FROM lookuptable_court WHERE UPPER(CONCAT(courttype, ' ', courtlocation)) LIKE UPPER('%'||:searchStr||'%')")
  Flux<CourtDTO> findBySearchStr(String searchStr);
}
