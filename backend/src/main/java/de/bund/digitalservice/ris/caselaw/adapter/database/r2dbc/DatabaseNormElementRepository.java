package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DatabaseNormElementRepository extends R2dbcRepository<NormElementDTO, Long> {

  @Query(
      "SELECT ne.id, ne.label, ne.has_number_designation, ne.norm_code "
          + "FROM norm_element ne "
          + "INNER JOIN document_category dc ON ne.document_category_id=dc.id "
          + "WHERE dc.label = 'R'")
  Flux<NormElementDTO> findAllByDocumentCategoryLabelR();
}
