package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseProcedureLinkRepository extends JpaRepository<ProcedureLinkDTO, UUID> {

  String QUERY_ACTIVE_PROCEDURE_LINKS_BY_PROCEDURE =
      "FROM procedure_link pl "
          + "JOIN ( "
          + "    SELECT documentation_unit_id, MAX(created_at) as latest_time "
          + "    FROM procedure_link "
          + "    GROUP BY documentation_unit_id "
          + ") subq "
          + "ON pl.documentation_unit_id = subq.documentation_unit_id AND pl.created_at = subq.latest_time";

  ProcedureLinkDTO findFirstByDocumentationUnitIdOrderByCreatedAtDesc(UUID documentationUnitId);

  List<ProcedureLinkDTO> findAllByDocumentationUnitIdOrderByCreatedAtDesc(UUID documentationUnitId);

  @Query(
      value =
          "SELECT pl.* "
              + QUERY_ACTIVE_PROCEDURE_LINKS_BY_PROCEDURE
              + " WHERE pl.procedure_id = :procedureId",
      nativeQuery = true)
  List<ProcedureLinkDTO> findLatestProcedureLinksByProcedure(
      @Param("procedureId") UUID procedureId);

  @Query(
      value =
          "SELECT COUNT(*) "
              + QUERY_ACTIVE_PROCEDURE_LINKS_BY_PROCEDURE
              + " WHERE pl.procedure_id = :procedureId",
      nativeQuery = true)
  Integer countLatestProcedureLinksByProcedure(@Param("procedureId") UUID procedureId);
}
