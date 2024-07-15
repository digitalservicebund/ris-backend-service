package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabaseHandoverReportRepository extends JpaRepository<HandoverReportDTO, UUID> {
  List<HandoverReportDTO> findAllByDocumentUnitId(UUID documentUnitId);
}
