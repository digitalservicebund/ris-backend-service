package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DocumentationUnitProcessStepRepository {

  DocumentationUnitProcessStep saveProcessStep(
      UUID documentationUnitId, UUID processStepId, UUID userId);

  Optional<DocumentationUnitProcessStep> getCurrentProcessStep(UUID documentationUnitId);

  List<DocumentationUnitProcessStep> findAllByDocumentationUnitId(UUID documentationUnitId);
}
