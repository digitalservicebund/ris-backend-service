package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DocumentationUnitProcessStepRepository {

  DocumentationUnitProcessStep findTopByDocumentationUnitIdOrderByCreatedAtDesc(
      UUID documentationUnitId);

  List<DocumentationUnitProcessStep> findByDocumentationUnitOrderByCreatedAtDesc(
      UUID documentationUnitId);

  DocumentationUnitProcessStep saveProcessStep(UUID documentationUnitId, UUID processStepId);
}
