package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentationUnitHistoryLogService {
  private final DocumentationUnitHistoryLogRepository repository;

  public DocumentationUnitHistoryLogService(DocumentationUnitHistoryLogRepository repository) {
    this.repository = repository;
  }

  public List<HistoryLog> getHistoryLogs(UUID uuid) {
    return repository.findByDocumentationUnitId(uuid);
  }
}
