package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DatabaseCitationStyleRepository extends R2dbcRepository<CitationStyleDTO, Long> {

  Flux<CitationStyleDTO> findAllByDocumentTypeAndCitationDocumentTypeOrderByCitationDocumentTypeAsc(
      char documentType, char citationDocumentType);

  @Query(
      "SELECT * FROM citation_style WHERE label LIKE :searchStr || '%' AND document_type = 'R' AND citation_document_type = 'R'")
  Flux<CitationStyleDTO> findBySearchStr(String searchStr);
}
