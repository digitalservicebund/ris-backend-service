package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseDocumentationUnitHistoryLogRepository
    extends JpaRepository<HistoryLogDTO, UUID> {
  List<HistoryLogDTO> findByDocumentationUnitIdOrderByCreatedAtDesc(UUID documentationUnitId);

  Optional<HistoryLogDTO> findFirstByDocumentationUnitIdAndUserNameAndEventTypeAndCreatedAtBetween(
      UUID documentationUnitId,
      String userName,
      HistoryLogEventType eventType,
      Instant start,
      Instant end);
}
