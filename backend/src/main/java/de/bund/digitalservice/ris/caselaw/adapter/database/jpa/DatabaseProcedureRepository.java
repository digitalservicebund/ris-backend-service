package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for accessing ProcedureDTO entities in the database. Extends JpaRepository
 * to provide basic CRUD operations.
 */
@Repository
public interface DatabaseProcedureRepository extends JpaRepository<ProcedureDTO, UUID> {

  /**
   * Finds all ProcedureDTOs by label containing a specific string and by documentation office,
   * ordered by creation date in descending order.
   *
   * @param label the label to search for, can be null
   * @param documentationOfficeDTO the documentation office to filter by
   * @param pageable the pagination information
   * @return a page of ProcedureDTOs matching the criteria
   */
  @Query(
      "SELECT p FROM ProcedureDTO p WHERE (:label IS NULL OR p.label LIKE %:label%) AND p.documentationOffice = :documentationOffice ORDER BY createdAt DESC NULLS LAST")
  Page<ProcedureDTO> findAllByLabelContainingAndDocumentationOfficeOrderByCreatedAtDesc(
      @Param("label") String label,
      @Param("documentationOffice") DocumentationOfficeDTO documentationOfficeDTO,
      Pageable pageable);

  /**
   * Finds all ProcedureDTOs by documentation office, ordered by creation date in descending order.
   *
   * @param documentationOfficeDTO the documentation office to filter by
   * @param pageable the pagination information
   * @return a page of ProcedureDTOs matching the criteria
   */
  @Query(
      "SELECT p FROM ProcedureDTO p WHERE p.documentationOffice = :documentationOffice ORDER BY createdAt DESC NULLS LAST")
  Page<ProcedureDTO> findAllByDocumentationOfficeOrderByCreatedAtDesc(
      @Param("documentationOffice") DocumentationOfficeDTO documentationOfficeDTO,
      Pageable pageable);

  /**
   * Finds a ProcedureDTO by label and documentation office.
   *
   * @param label the label to search for
   * @param documentationUnitDTO the documentation office to filter by
   * @return an Optional containing the found ProcedureDTO, or empty if not found
   */
  Optional<ProcedureDTO> findAllByLabelAndDocumentationOffice(
      String label, DocumentationOfficeDTO documentationUnitDTO);

  /**
   * Retrieves a paginated list of distinct ProcedureDTO entities filtered by label and
   * documentation office, ensuring that only the procedure which is used in a documentation unit
   * and has the highest rank is selected.
   *
   * @param label The label to filter procedures by, nullable.
   * @param documentationOfficeDTO The documentation office to filter procedures by.
   * @param pageable Pagination information.
   * @return A paginated list of filtered ProcedureDTO entities.
   */
  @Query(
      "SELECT DISTINCT p FROM ProcedureDTO p "
          + "JOIN DocumentationUnitProcedureDTO dup ON p.id = dup.procedure.id "
          + "WHERE (:label IS NULL OR p.label LIKE %:label%) "
          + "AND p.documentationOffice = :documentationOffice "
          + "AND ( dup.documentationUnit, dup.rank) IN ("
          + "    SELECT  dupMax.documentationUnit.id, MAX( dupMax.rank) "
          + "    FROM DocumentationUnitProcedureDTO dupMax "
          + "    GROUP BY  dupMax.documentationUnit.id)"
          + "    ORDER BY  p.createdAt DESC NULLS LAST")
  Page<ProcedureDTO> findLatestUsedProceduresByLabelAndDocumentationOffice(
      @Param("label") String label,
      @Param("documentationOffice") DocumentationOfficeDTO documentationOfficeDTO,
      Pageable pageable);

  /**
   * Retrieves a paginated list of distinct ProcedureDTO entities filtered by documentation office,
   * ensuring that only the procedure with the highest rank for each documentation unit is selected.
   *
   * @param documentationOffice The documentation office to filter procedures by.
   * @param pageable Pagination information.
   * @return A paginated list of filtered ProcedureDTO entities.
   */
  @Query(
      "SELECT DISTINCT p FROM ProcedureDTO p "
          + "JOIN DocumentationUnitProcedureDTO dup ON p.id = dup.procedure.id "
          + "WHERE p.documentationOffice = :documentationOffice "
          + "AND ( dup.documentationUnit, dup.rank) IN ("
          + "    SELECT  dupMax.documentationUnit.id, MAX( dupMax.rank) "
          + "    FROM DocumentationUnitProcedureDTO dupMax "
          + "    GROUP BY  dupMax.documentationUnit.id)"
          + "    ORDER BY  p.createdAt DESC NULLS LAST")
  Page<ProcedureDTO> findLatestUsedProceduresByDocumentationOffice(
      @Param("documentationOffice") DocumentationOfficeDTO documentationOffice, Pageable pageable);
}
