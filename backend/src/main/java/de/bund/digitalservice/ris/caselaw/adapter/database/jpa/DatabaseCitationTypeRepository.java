package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseCitationTypeRepository extends JpaRepository<CitationTypeDTO, Long> {

  @Query(
      "SELECT ct FROM CitationTypeDTO ct WHERE ct.documentationUnitDocumentCategory.label = 'R' "
          + "AND ct.citationDocumentCategory.label = 'R'"
          + "ORDER BY ct.label")
  CitationTypeDTO findAllByDocumentTypes(char documentType, char citationDocumentType);

  @Query(
      "SELECT ct FROM CitationTypeDTO ct WHERE LOWER(ct.label) LIKE concat(LOWER(:searchStr), '%')"
          + "AND ct.documentationUnitDocumentCategory.label = 'R' AND ct.citationDocumentCategory.label = 'R' "
          + "ORDER BY ct.label")
  CitationTypeDTO findBySearchStr(String searchStr);
}
