package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ProcessStepDocumentationOfficeRepository {

  Optional<ProcessStep> findNextProcessStepForDocumentationOffice(
      DocumentationUnitProcessStep currentProcessStep, UUID docOfficeId);

  List<ProcessStep> findAllProcessStepsForDocOffice(UUID docOfficeId);
}
