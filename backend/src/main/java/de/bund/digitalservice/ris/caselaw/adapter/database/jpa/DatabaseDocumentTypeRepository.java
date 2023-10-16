package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/** */
@Repository
public interface DatabaseDocumentTypeRepository extends JpaRepository<DocumentTypeDTO, UUID> {

  DocumentTypeDTO findFirstByAbbreviationAndCategory(
      String jurisShortcut, DocumentCategoryDTO category);

  List<DocumentTypeDTO> findAllByCategoryOrderByAbbreviationAscLabelAsc(
      DocumentCategoryDTO category);

  // see query explanation in CourtRepository, it's almost the same
  @Query(
      nativeQuery = true,
      value =
          "WITH label_added AS (SELECT *, "
              + " UPPER(CONCAT(abbreviation, ' ', label)) AS concat"
              + " from incremental_migration.document_type) "
              + "SELECT *,"
              + "       concat, "
              + "       CASE "
              + "           WHEN concat LIKE UPPER(:searchStr||'%') THEN 1 "
              + "           WHEN concat LIKE UPPER('% '||:searchStr||'%') THEN 2 "
              + "           WHEN concat LIKE UPPER('%-'||:searchStr||'%') THEN 2 "
              + "           ELSE 3 "
              + "           END AS weight "
              + "FROM label_added "
              + "WHERE concat LIKE UPPER('%'||:searchStr||'%') AND document_category_id = :category "
              + "ORDER BY weight, concat")
  List<DocumentTypeDTO> findCaselawBySearchStrAndCategory(String searchStr, UUID category);
}
