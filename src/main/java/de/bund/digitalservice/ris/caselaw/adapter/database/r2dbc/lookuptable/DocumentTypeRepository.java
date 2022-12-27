package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DocumentTypeRepository extends ReactiveSortingRepository<DocumentTypeDTO, Long> {

  Flux<DocumentTypeDTO> findAllByDocumentTypeOrderByJurisShortcutAscLabelAsc(char documentType);

  @Query(
      "SELECT * FROM lookuptable_documenttype WHERE UPPER(CONCAT(juris_shortcut, ' ', label)) LIKE UPPER('%'||:searchStr||'%') AND document_type = 'R' ORDER BY juris_shortcut, label")
  Flux<DocumentTypeDTO> findCaselawBySearchStr(String searchStr);
}
