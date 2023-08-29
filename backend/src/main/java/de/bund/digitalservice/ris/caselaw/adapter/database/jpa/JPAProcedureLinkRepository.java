package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JPAProcedureLinkRepository extends JpaRepository<JPAProcedureLinkDTO, UUID> {
  JPAProcedureLinkDTO findFirstByDocumentationUnitIdOrderByCreatedAtDesc(UUID documentationUnitId);

  List<JPAProcedureLinkDTO> findAllByDocumentationUnitIdOrderByCreatedAtDesc(
      UUID documentationUnitId);

  @Query(
      value =
          "SELECT pl.* "
              + "FROM procedure_link pl "
              + "JOIN ( "
              + "    SELECT documentation_unit_id, MAX(created_at) as latest_time "
              + "    FROM procedure_link "
              + "    GROUP BY documentation_unit_id "
              + ") subq "
              + "ON pl.documentation_unit_id = subq.documentation_unit_id AND pl.created_at = subq.latest_time "
              + "WHERE pl.procedure_id = :procedureId",
      nativeQuery = true)
  List<JPAProcedureLinkDTO> findLatestProcedureLinksByProcedure(
      @Param("procedureId") UUID procedureId);
}
