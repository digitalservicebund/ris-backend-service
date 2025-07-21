package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitProcessStepTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ProcessStepTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStepRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/** Implementation of the DocumentationUnitRepository for the Postgres database */
@Repository
@Slf4j
@Primary
public class PostgresDocumentationUnitProcessStepRepositoryImpl
    implements DocumentationUnitProcessStepRepository {

  private final DatabaseDocumentationUnitProcessStepRepository repository;
  private final DatabaseProcessStepRepository processStepRepository;

  public PostgresDocumentationUnitProcessStepRepositoryImpl(
      DatabaseDocumentationUnitProcessStepRepository repository,
      DatabaseProcessStepRepository processStepRepository) {

    this.repository = repository;
    this.processStepRepository = processStepRepository;
  }

  @Override
  public Optional<DocumentationUnitProcessStep> findTopByDocumentationUnitIdOrderByCreatedAtDesc(
      UUID documentationUnitId) {

    return repository
        .findTopByDocumentationUnitIdOrderByCreatedAtDesc(documentationUnitId)
        .map(
            entity -> {
              ProcessStepDTO processStepDTO =
                  processStepRepository
                      .findById(entity.getProcessStepId())
                      .orElseThrow(
                          () ->
                              new EntityNotFoundException(
                                  "Associated ProcessStep with ID "
                                      + entity.getProcessStepId()
                                      + " not found for ProcessStepEntity "
                                      + entity.getId()));

              return DocumentationUnitProcessStepTransformer.toDomain(
                  entity, ProcessStepTransformer.toDomain(processStepDTO));
            });
  }

  @Override
  public List<DocumentationUnitProcessStep> findByDocumentationUnitOrderByCreatedAtDesc(
      UUID documentationUnitId) {
    return repository.findByDocumentationUnitIdOrderByCreatedAtDesc(documentationUnitId).stream()
        .map(
            entity -> {
              ProcessStepDTO processStepDTO =
                  processStepRepository
                      .findById(entity.getProcessStepId())
                      .orElseThrow(
                          () ->
                              new EntityNotFoundException(
                                  "Associated ProcessStep with ID "
                                      + entity.getProcessStepId()
                                      + " not found for ProcessStepEntity "
                                      + entity.getId()));
              return DocumentationUnitProcessStepTransformer.toDomain(
                  entity, ProcessStepTransformer.toDomain(processStepDTO));
            })
        .collect(Collectors.toList());
  }
}
