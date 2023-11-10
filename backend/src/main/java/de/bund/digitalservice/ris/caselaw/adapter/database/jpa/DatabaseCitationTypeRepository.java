package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseCitationTypeRepository extends JpaRepository<CitationTypeDTO, Long> {
  @Query(
      "SELECT ct FROM CitationTypeDTO ct WHERE LOWER(ct.label) LIKE concat(LOWER(:searchStr), '%')"
          + "AND ct.documentationUnitDocumentCategory.label = 'R' AND ct.citationDocumentCategory.label = 'R' "
          + "ORDER BY ct.label")
  List<CitationTypeDTO> findBySearchStr(String searchStr);

  @Query(
      "SELECT ct FROM CitationTypeDTO ct WHERE ct.citationDocumentCategory.label = ':categoryLabel' "
          + "ORDER BY ct.abbreviation")
  List<CitationTypeDTO> findAllByCitationDocumentCategoryOrderByAbbreviation(String categoryLabel);
}
