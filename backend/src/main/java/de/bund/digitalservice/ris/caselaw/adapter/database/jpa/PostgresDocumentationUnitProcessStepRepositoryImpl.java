package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationUnitProcessStepTransformer;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.ProcessStepTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.domain.exception.ProcessStepNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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

  private DocumentationUnitProcessStep transformDocumentationUnitProcessStep(
      DocumentationUnitProcessStepDTO dto) {
    ProcessStepDTO processStepDTO =
        processStepRepository
            .findById(dto.getProcessStepId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Prozessschritt für ID "
                            + dto.getProcessStepId()
                            + " wurde nicht gefunden für DocumentationUnitProcessStepDTO "
                            + dto.getId()));

    return DocumentationUnitProcessStepTransformer.toDomain(
        dto, ProcessStepTransformer.toDomain(processStepDTO));
  }

  @Override
  public DocumentationUnitProcessStep findTopByDocumentationUnitIdOrderByCreatedAtDesc(
      UUID documentationUnitId) {

    return repository
        .findTopByDocumentationUnitIdOrderByCreatedAtDesc(documentationUnitId)
        .map(this::transformDocumentationUnitProcessStep)
        .orElseThrow(
            () ->
                new ProcessStepNotFoundException(
                    "Für Dokeinheit mit ID: "
                        + documentationUnitId
                        + " wurde kein Prozessschritt gefunden, obwohl einer erwartet wurde."));
  }

  @Override
  public List<DocumentationUnitProcessStep> findByDocumentationUnitOrderByCreatedAtDesc(
      UUID documentationUnitId) {

    return repository.findByDocumentationUnitIdOrderByCreatedAtDesc(documentationUnitId).stream()
        .map(this::transformDocumentationUnitProcessStep)
        .collect(Collectors.toList());
  }

  @Override
  public DocumentationUnitProcessStep saveProcessStep(
      UUID documentationUnitId, UUID processStepId) {
    DocumentationUnitProcessStepDTO newDTO =
        DocumentationUnitProcessStepDTO.builder()
            // Todo:  .userId
            .createdAt(LocalDateTime.now())
            .processStepId(processStepId)
            .documentationUnitId(documentationUnitId)
            .build();
    return transformDocumentationUnitProcessStep(repository.save(newDTO));
  }
}
