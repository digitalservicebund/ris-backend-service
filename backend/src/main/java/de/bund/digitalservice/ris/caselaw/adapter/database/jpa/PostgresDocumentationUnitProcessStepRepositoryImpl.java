package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitProcessStepTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStepRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/** Implementation of the DocumentationUnitProcessStepRepository for the Postgres database */
@Repository
@Slf4j
@Primary
public class PostgresDocumentationUnitProcessStepRepositoryImpl
    implements DocumentationUnitProcessStepRepository {

  private final DatabaseDocumentationUnitProcessStepRepository repository;

  public PostgresDocumentationUnitProcessStepRepositoryImpl(
      DatabaseDocumentationUnitProcessStepRepository repository) {

    this.repository = repository;
  }

  @Override
  public Optional<DocumentationUnitProcessStep> getCurrentProcessStep(UUID documentationUnitId) {

    return repository
        .findTopByDocumentationUnitIdOrderByCreatedAtDesc(documentationUnitId)
        .map(DocumentationUnitProcessStepTransformer::toDomain);
  }

}
