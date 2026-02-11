package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseNormElementRepository extends JpaRepository<NormElementDTO, UUID> {

  @Query(
      value =
          "SELECT ne.id, ne.label, ne.has_number_designation, ne.norm_code "
              + "FROM norm_element ne "
              + "INNER JOIN document_category dc ON ne.document_category_id=dc.id "
              + "WHERE dc.label = 'R'",
      nativeQuery = true)
  List<NormElementDTO> findAllByDocumentCategoryLabelR();
}
