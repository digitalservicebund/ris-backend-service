package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.ProcessStepTransformer;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStepRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/** Implementation of the ProcessStepRepository for the Postgres database */
@Repository
@Slf4j
@Primary
public class PostgresProcessStepRepositoryImpl implements ProcessStepRepository {

  private final DatabaseProcessStepRepository repository;

  public PostgresProcessStepRepositoryImpl(DatabaseProcessStepRepository repository) {

    this.repository = repository;
  }

  @Override
  public Optional<ProcessStep> findByName(String name) {
    return repository.findByName(name).map(ProcessStepTransformer::toDomain);
  }
}
