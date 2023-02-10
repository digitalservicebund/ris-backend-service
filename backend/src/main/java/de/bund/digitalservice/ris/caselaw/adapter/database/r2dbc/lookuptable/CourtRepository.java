package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CourtRepository extends R2dbcRepository<CourtDTO, Long> {

  Mono<CourtDTO> findByCourttypeAndCourtlocation(String courttype, String courtlocation);

  Flux<CourtDTO> findAllByOrderByCourttypeAscCourtlocationAsc();

  /*
  The query gets all rows where searchStr is anywhere in the label.
  The CASE statements are used to order the results into 3 priority classes:

  1. searchStr is start of label
  2. searchStr is start of a word within label: indicated by being after a space or a dash
  3. that leaves the else case: searchStr is anywhere in the string

  The order of the CASE statements is important. If the 3rd would be first, there would only be
  results of priority 3.
  Within a priority class, ordering is alphabetical.
  */
  @Query(
      "WITH label_added AS (SELECT *, "
          + "                            UPPER(CONCAT(courttype, ' ', courtlocation)) AS label "
          + "                     from lookuptable_court) "
          + "SELECT *,"
          + "       label, "
          + "       CASE "
          + "           WHEN label LIKE UPPER(:searchStr||'%') THEN 1 "
          + "           WHEN label LIKE UPPER('% '||:searchStr||'%') THEN 2 "
          + "           WHEN label LIKE UPPER('%-'||:searchStr||'%') THEN 2 "
          + "           ELSE 3 "
          + "           END AS weight "
          + "FROM label_added "
          + "WHERE label LIKE UPPER('%'||:searchStr||'%') "
          + "ORDER BY weight, label")
  Flux<CourtDTO> findBySearchStr(String searchStr);
}
