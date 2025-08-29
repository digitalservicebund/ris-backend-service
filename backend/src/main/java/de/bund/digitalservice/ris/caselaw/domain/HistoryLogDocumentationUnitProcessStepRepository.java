package de.bund.digitalservice.ris.caselaw.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface HistoryLogDocumentationUnitProcessStepRepository {

  /**
   * Finds a documentation unit process step by its history log ID.
   *
   * @param historyLogId The ID of the history log.
   * @return An Optional containing the documentation unit process step, or empty if not found.
   */
  Optional<HistoryLogDocumentationUnitProcessStep> findByHistoryLogId(UUID historyLogId);
}
