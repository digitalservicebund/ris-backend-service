package de.bund.digitalservice.ris.caselaw.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DocumentationUnitProcessStepRepository {

  Optional<DocumentationUnitProcessStep> getCurrentProcessStep(UUID documentationUnitId);
}
