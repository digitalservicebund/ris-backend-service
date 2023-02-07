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
  CONCAT(courttype, ' ', courtlocation) will be displayed in the dropdown in the frontend

  The query gets all rows where searchStr is anywhere in CONCAT. The CASE statements are then
  used to order the results into 3 priority classes.
  Within a priority class, ordering is alphabetical.

  ^ marks the beginning --> searchStr is start of entire string
  \m is a word boundary --> searchStr is start of a word
  .* is any character --> searchStr is anywhere in the string

  The order of the CASE statements is important. If the 3rd would be first, there would only be
  results of priority 3.

  Note: ~* is case-insensitive, ~ would be case-sensitive
  */
  @Query(
      "SELECT *, "
          + "CASE "
          + "  WHEN CONCAT(courttype, ' ', courtlocation) ~* CONCAT('^', :searchStr) THEN 1 "
          + "  WHEN CONCAT(courttype, ' ', courtlocation) ~* CONCAT('\\m', :searchStr) THEN 2 "
          + "  WHEN CONCAT(courttype, ' ', courtlocation) ~* CONCAT('.*', :searchStr, '.*') THEN 3 "
          + "  END AS priority "
          + "FROM lookuptable_court "
          + "WHERE CONCAT(courttype, ' ', courtlocation) ~* CONCAT('.*', :searchStr, '.*') "
          + "ORDER BY priority, CONCAT(courttype, ' ', courtlocation)")
  Flux<CourtDTO> findBySearchStr(String searchStr);
}
