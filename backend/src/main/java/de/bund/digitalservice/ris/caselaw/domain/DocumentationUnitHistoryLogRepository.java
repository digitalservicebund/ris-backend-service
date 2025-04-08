package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DocumentationUnitHistoryLogRepository {
  List<HistoryLog> findByDocumentationUnitId(UUID uuid);
}
