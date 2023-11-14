package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DatabasePublicationReportRepository
    extends JpaRepository<PublicationReportDTO, UUID> {
  List<PublicationReportDTO> findAllByDocumentUnitId(UUID documentUnitId);
}
