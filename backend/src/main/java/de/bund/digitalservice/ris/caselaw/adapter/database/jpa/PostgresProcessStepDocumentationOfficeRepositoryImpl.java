package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.ProcessStepTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStepDocumentationOfficeRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@Primary
public class PostgresProcessStepDocumentationOfficeRepositoryImpl
    implements ProcessStepDocumentationOfficeRepository {

  private final DatabaseProcessStepDocumentationOfficeRepository databaseRepository;
  private final DatabaseProcessStepRepository databaseProcessStepRepository;

  public PostgresProcessStepDocumentationOfficeRepositoryImpl(
      DatabaseProcessStepDocumentationOfficeRepository databaseRepository,
      DatabaseProcessStepRepository databaseProcessStepRepository) {
    this.databaseRepository = databaseRepository;
    this.databaseProcessStepRepository = databaseProcessStepRepository;
  }

  @Override
  public Optional<ProcessStep> findNextProcessStepForDocumentationOffice(
      DocumentationUnitProcessStep documentationUnitProcessStep, UUID docOfficeId) {

    ProcessStep currentProcessStep = documentationUnitProcessStep.getProcessStep();

    Optional<ProcessStepDocumentationOfficeDTO> nextProcessStepDocumentationOfficeDTO =
        databaseRepository.findNextByProcessStepIdAndDocumentationOfficeId(
            currentProcessStep.uuid(), docOfficeId);

    return nextProcessStepDocumentationOfficeDTO.map(
        nextStep -> {
          ProcessStepDTO processStepDTO =
              databaseProcessStepRepository
                  .findById(nextStep.getProcessStepId())
                  .orElseThrow(
                      () ->
                          new EntityNotFoundException(
                              "Associated ProcessStep with ID "
                                  + nextStep.getProcessStepId()
                                  + " not found for ProcessStepDocumentationOfficeDTO "
                                  + nextStep.getId()));

          return ProcessStepTransformer.toDomain(processStepDTO);
        });
  }

  @Override
  public List<ProcessStep> findAllProcessStepsForDocOffice(UUID docOfficeId) {
    List<ProcessStepDocumentationOfficeDTO> entities =
        databaseRepository.findByDocumentationOfficeIdOrderByRankAsc(docOfficeId);

    return entities.stream()
        .map(
            entity -> {
              ProcessStepDTO processStepDTO =
                  databaseProcessStepRepository
                      .findById(entity.getProcessStepId())
                      .orElseThrow(
                          () ->
                              new EntityNotFoundException(
                                  "ProcessStep with ID "
                                      + entity.getProcessStepId()
                                      + " not found for ProcessStepDocumentationOfficeEntity "
                                      + entity.getId()));
              return ProcessStepTransformer.toDomain(processStepDTO);
            })
        .collect(Collectors.toList());
  }
}
