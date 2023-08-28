package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JPAProcedureLinkRepository extends JpaRepository<JPAProcedureLinkDTO, UUID> {
  JPAProcedureLinkDTO findFirstByDocumentationUnitIdOrderByCreatedAtDesc(UUID documentationUnitId);

  List<JPAProcedureLinkDTO> findAllByDocumentationUnitIdOrderByCreatedAtDesc(
      UUID documentationUnitId);

  Integer countByProcedureDTO(JPAProcedureDTO procedureDTO);
}
