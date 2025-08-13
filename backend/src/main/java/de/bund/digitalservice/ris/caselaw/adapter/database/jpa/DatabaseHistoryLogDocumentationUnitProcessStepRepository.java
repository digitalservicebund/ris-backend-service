package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseHistoryLogDocumentationUnitProcessStepRepository
    extends JpaRepository<HistoryLogDocumentationUnitProcessStepDTO, UUID> {
  List<HistoryLogDocumentationUnitProcessStepDTO> findByHistoryLogIdIn(
      Set<UUID> logIdsWithProcessSteps);
}
