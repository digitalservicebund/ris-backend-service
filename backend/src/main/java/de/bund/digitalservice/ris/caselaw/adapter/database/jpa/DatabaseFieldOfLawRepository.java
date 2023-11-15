package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseFieldOfLawRepository extends JpaRepository<FieldOfLawDTO, UUID> {

  FieldOfLawDTO findByIdentifier(String identifier);

  @Query(
      "select fol from FieldOfLawDTO fol where fol.parent is null and fol.notation = 'NEW' order by fol.identifier")
  List<FieldOfLawDTO> findAllByParentIsNullAndNotationOrderByIdentifier();

  Page<FieldOfLawDTO> findAllByOrderByIdentifierAsc(Pageable pageable);

  @Query(
      "select fol from FieldOfLawDTO fol where  fol.notation = 'NEW' and fol.identifier like upper(concat('%', :searchStr, '%')) order by fol.identifier")
  List<FieldOfLawDTO> findAllByIdentifierContainingIgnoreCaseOrderByIdentifier(String searchStr);

  @Query(
      "SELECT f FROM FieldOfLawDTO f WHERE f.notation = 'NEW' AND "
          + "(UPPER(f.identifier) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR "
          + "UPPER(f.text) LIKE UPPER(CONCAT('%', :searchTerm, '%')))")
  List<FieldOfLawDTO> findAllByNotationAndIdentifierContainingIgnoreCaseOrTextContainingIgnoreCase(
      String searchTerm);
}
