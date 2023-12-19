package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseCourtRepository extends JpaRepository<CourtDTO, UUID> {

  List<CourtDTO> findAllByOrderByTypeAscLocationAsc();

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
      nativeQuery = true,
      value =
          "WITH label_added AS (SELECT *, "
              + "                            UPPER(CONCAT(type, ' ', location)) AS label "
              + "                     from incremental_migration.court) "
              + "SELECT *,"
              + "       label, "
              + "       CASE "
              + "           WHEN label LIKE UPPER(:searchStr||'%') THEN 1 "
              + "           WHEN label LIKE UPPER('% '||:searchStr||'%') THEN 2 "
              + "           WHEN label LIKE UPPER('%-'||:searchStr||'%') THEN 2 "
              + "           ELSE 3 "
              + "           END AS weight "
              + "FROM label_added as court "
              // FIXME: this inner join has been added to fill the region field in court. This
              // should be solved in other ways with better performance (e.g. change query or use a
              // Interface instead of the full CourtDTO
              + "LEFT JOIN incremental_migration.court_region as region on court.id = region.court_id  "
              + "WHERE label LIKE UPPER('%'||:searchStr||'%') "
              + "ORDER BY weight, label")
  List<CourtDTO> findBySearchStr(@Param("searchStr") String searchStr);
}
