package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStepRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/** Implementation of the DocumentationUnitRepository for the Postgres database */
@Repository
@Slf4j
@Primary
// Repository for main entity -> depends on more than 20 classes :-/
@SuppressWarnings("java:S6539")
public class PostgresProcessStepRepositoryImpl implements ProcessStepRepository {

  private final DatabaseProcessStepRepository processStepRepository;

  public PostgresProcessStepRepositoryImpl(DatabaseProcessStepRepository processStepRepository) {

    this.processStepRepository = processStepRepository;
  }

  @Override
  public Optional<ProcessStep> findById(UUID processStepId) {
    return processStepRepository.findById(processStepId);
  }
}
