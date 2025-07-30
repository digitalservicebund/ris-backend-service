package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitProcessStepTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.domain.exception.ProcessStepNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
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
  private final DatabaseProcessStepRepository processStepRepository;

  public PostgresDocumentationUnitProcessStepRepositoryImpl(
      DatabaseDocumentationUnitProcessStepRepository repository,
      DatabaseProcessStepRepository processStepRepository) {

    this.repository = repository;
    this.processStepRepository = processStepRepository;
  }

  @Override
  public DocumentationUnitProcessStep saveProcessStep(
      UUID documentationUnitId, UUID processStepId, UUID userId) {
    var processStep =
        processStepRepository
            .findById(processStepId)
            .orElseThrow(
                () ->
                    new ProcessStepNotFoundException(
                        "Prozessschritt für ID " + processStepId + " wurde nicht gefunden."));
    DocumentationUnitProcessStepDTO newDTO =
        DocumentationUnitProcessStepDTO.builder()
            .createdAt(LocalDateTime.now())
            .processStep(processStep)
            .userId(userId)
            .build();

    return DocumentationUnitProcessStepTransformer.toDomain(repository.save(newDTO));
  }

  @Override
  public Optional<DocumentationUnitProcessStep> getCurrentProcessStep(UUID documentationUnitId) {

    return repository
        .findTopByDocumentationUnitIdOrderByCreatedAtDesc(documentationUnitId)
        .map(DocumentationUnitProcessStepTransformer::toDomain);
  }

  @Override
  public List<DocumentationUnitProcessStep> findAllByDocumentationUnitId(UUID documentationUnitId) {

    return repository.findByDocumentationUnitIdOrderByCreatedAtDesc(documentationUnitId).stream()
        .map(DocumentationUnitProcessStepTransformer::toDomain)
        .toList();
  }
}
