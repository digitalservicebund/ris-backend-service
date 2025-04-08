package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogRepository;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLog;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresDocumentationUnitHistoryLogRepositoryImpl
    implements DocumentationUnitHistoryLogRepository {

  private final DatabaseDocumentationUnitHistoryLogRepository databaseRepository;

  public PostgresDocumentationUnitHistoryLogRepositoryImpl(
      DatabaseDocumentationUnitHistoryLogRepository databaseRepository) {
    this.databaseRepository = databaseRepository;
  }

  @Override
  public List<HistoryLog> findByDocumentationUnitId(UUID documentationUnitId) {
    return databaseRepository.findByDocumentationUnitId(documentationUnitId).stream()
        .map(this::toDomain)
        .toList();
  }

  private HistoryLog toDomain(HistoryLogDTO dto) {
    return new HistoryLog(
        dto.getId(),
        dto.getCreatedAt(),
        dto.getDocumentationUnitId(),
        dto.getDocumentationOffice(),
        dto.getUserId(),
        dto.getUserName(),
        dto.getSystemName(),
        dto.getDescription(),
        dto.getEventType());
  }
}
