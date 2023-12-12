package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseDocumentationUnitProcedureRepository
    extends JpaRepository<DocumentationUnitProcedureDTO, UUID> {

  @Query("SELECT pl FROM DocumentationUnitProcedureDTO pl ORDER BY pl.primaryKey.rank ASC LIMIT 1")
  DocumentationUnitProcedureDTO findFirstByDocumentationUnitOrderByRankDesc(
      DocumentationUnitDTO documentationUnitDTO);

  //  String QUERY_ACTIVE_PROCEDURE_LINKS_BY_PROCEDURE =
  //      "FROM procedure_link pl "
  //          + "JOIN ( "
  //          + "    SELECT documentation_unit_id, MAX(rank) as highest_rank "
  //          + "    FROM procedure_link "
  //          + "    GROUP BY documentation_unit_id "
  //          + ") subq "
  //          + "ON pl.documentation_unit_id = subq.documentation_unit_id AND pl.rank =
  // subq.highest_rank";
  //
  //  ProcedureLinkDTO findFirstByDocumentationUnitIdOrderByRankDesc(
  //      UUID documentationUnitId);
  //
  //  List<ProcedureLinkDTO> findAllByDocumentationUnitIdOrderByRankDesc(
  //      UUID documentationUnitId);
  //
  //  @Query(
  //      value =
  //          "SELECT pl.* "
  //              + QUERY_ACTIVE_PROCEDURE_LINKS_BY_PROCEDURE
  //              + " WHERE pl.procedure_id = :procedureId",
  //      nativeQuery = true)
  //  List<ProcedureLinkDTO> findLatestProcedureLinksByProcedure(
  //      @Param("procedureId") UUID procedureId);
  //
  //  @Query(
  //      value =
  //          "SELECT COUNT(*) "
  //              + QUERY_ACTIVE_PROCEDURE_LINKS_BY_PROCEDURE
  //              + " WHERE pl.procedure_id = :procedureId",
  //      nativeQuery = true)
  //  Integer countLatestProcedureLinksByProcedure(@Param("procedureId") UUID procedureId);
}
