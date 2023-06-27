package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DatabaseCitationStyleRepository extends R2dbcRepository<CitationStyleDTO, Long> {

  Flux<CitationStyleDTO> findAllByDocumentTypeAndCitationDocumentTypeOrderByLabelAsc(
      char documentType, char citationDocumentType);

  @Query(
      "SELECT * FROM citation_style WHERE LOWER(label) LIKE LOWER(:searchStr) || '%' "
          + "AND document_type = 'R' AND citation_document_type = 'R' "
          + "ORDER BY label")
  Flux<CitationStyleDTO> findBySearchStr(String searchStr);

  Mono<CitationStyleDTO> findByUuid(UUID citationStyleUuid);
}
