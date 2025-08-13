package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.HistoryLogDocumentationUnitProcessStepDTO;
import org.springframework.data.repository.NoRepositoryBean;

/** Domain repository for documentation units */
@NoRepositoryBean
public interface HistoryLogDocumentationUnitProcessStepRepository {
  void save(HistoryLogDocumentationUnitProcessStepDTO mappingDto);
}
