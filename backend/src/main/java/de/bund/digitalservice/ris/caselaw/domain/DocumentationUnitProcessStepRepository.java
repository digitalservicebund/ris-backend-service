package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DocumentationUnitProcessStepRepository {

  /**
   * Find the latest (current) process step of a documentation unit by documentationUnitId
   *
   * @param documentationUnitId the uuid of the documentation unit
   * @return the latest (current) process step of the documentation unit
   */
  Optional<DocumentationUnitProcessStep> findTopByDocumentationUnitIdOrderByCreatedAtDesc(
      UUID documentationUnitId);

  List<DocumentationUnitProcessStep> findByDocumentationUnitOrderByCreatedAtDesc(
      UUID documentationUnitId);
}
