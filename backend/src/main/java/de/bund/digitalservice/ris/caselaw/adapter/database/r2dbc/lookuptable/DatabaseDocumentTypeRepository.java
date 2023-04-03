package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DatabaseDocumentTypeRepository extends R2dbcRepository<DocumentTypeDTO, Long> {

  Mono<DocumentTypeDTO> findByJurisShortcut(String jurisShortcut);

  Flux<DocumentTypeDTO> findAllByDocumentTypeOrderByJurisShortcutAscLabelAsc(char documentType);

  // see query explanation in CourtRepository, it's almost the same
  @Query(
      "WITH label_added AS (SELECT *, "
          + "                            UPPER(CONCAT(juris_shortcut, ' ', label)) AS concat"
          + "                     from lookuptable_documenttype) "
          + "SELECT *,"
          + "       concat, "
          + "       CASE "
          + "           WHEN concat LIKE UPPER(:searchStr||'%') THEN 1 "
          + "           WHEN concat LIKE UPPER('% '||:searchStr||'%') THEN 2 "
          + "           WHEN concat LIKE UPPER('%-'||:searchStr||'%') THEN 2 "
          + "           ELSE 3 "
          + "           END AS weight "
          + "FROM label_added "
          + "WHERE concat LIKE UPPER('%'||:searchStr||'%') AND document_type = 'R' "
          + "ORDER BY weight, concat")
  Flux<DocumentTypeDTO> findCaselawBySearchStr(String searchStr);
}
