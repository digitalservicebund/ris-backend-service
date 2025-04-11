package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

/** Domain repository for documentation units */
@NoRepositoryBean
public interface DocumentationUnitHistoryLogRepository {

  /**
   * Find all history logs for a documentation unit by its uuid
   *
   * @param uuid the uuid of the documentation unit
   * @return a list of history logs for this documentation unit
   */
  List<HistoryLog> findByDocumentationUnitId(UUID uuid, User user);
}
