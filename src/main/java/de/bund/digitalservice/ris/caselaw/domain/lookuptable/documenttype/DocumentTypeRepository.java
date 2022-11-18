package de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DocumentTypeRepository extends ReactiveSortingRepository<DocumentTypeDTO, Long> {

  @Query(
      "SELECT * FROM lookuptable_documenttype WHERE UPPER(CONCAT(juris_shortcut, ' ', label)) LIKE UPPER('%'||:searchStr||'%')")
  Flux<DocumentTypeDTO> findBySearchStr(String searchStr);
}
