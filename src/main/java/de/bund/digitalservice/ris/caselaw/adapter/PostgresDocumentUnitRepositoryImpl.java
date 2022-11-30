package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitBuilder;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class PostgresDocumentUnitRepositoryImpl implements DocumentUnitRepository {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(PostgresDocumentUnitRepositoryImpl.class);
  private final DatabaseDocumentUnitRepository repository;
  private final DatabasePreviousDecisionRepository previousDecisionRepository;

  public PostgresDocumentUnitRepositoryImpl(
      DatabaseDocumentUnitRepository repository,
      DatabasePreviousDecisionRepository previousDecisionRepository) {

    this.repository = repository;
    this.previousDecisionRepository = previousDecisionRepository;
  }

  @Override
  public Mono<DocumentUnit> findByDocumentNumber(String documentNumber) {
    return repository
        .findByDocumentnumber(documentNumber)
        .flatMap(this::addPreviousDecisions)
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build());
  }

  @Override
  public Mono<DocumentUnit> findByUuid(UUID uuid) {
    return repository
        .findByUuid(uuid)
        .flatMap(this::addPreviousDecisions)
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build());
  }

  @Override
  public Mono<DocumentUnit> createNewDocumentUnit(String documentNumber) {
    return repository
        .save(
            DocumentUnitDTO.builder()
                .uuid(UUID.randomUUID())
                .creationtimestamp(Instant.now())
                .documentnumber(documentNumber)
                .build())
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build());
  }

  @Override
  public Mono<DocumentUnit> save(DocumentUnit documentUnit) {
    return repository
        .save(DocumentUnitDTO.buildFromDocumentUnit(documentUnit))
        .flatMap(
            documentUnitDTO ->
                previousDecisionRepository
                    .deleteAllByDocumentUnitId(documentUnitDTO.getId())
                    .thenReturn(documentUnitDTO))
        .flatMap(
            documentUnitDTO -> {
              List<Long> existingIds =
                  documentUnit.previousDecisions().stream().map(PreviousDecision::id).toList();

              if (existingIds.isEmpty()) {
                return Mono.just(documentUnitDTO);
              }

              return previousDecisionRepository
                  .findAllById(existingIds)
                  .collectList()
                  .doOnNext(
                      list -> {
                        if (!list.isEmpty()) {
                          throw new RuntimeException(
                              "previous decision id exist for other document unit id");
                        }
                      })
                  .thenReturn(documentUnitDTO);
            })
        .flatMap(
            documentUnitDTO -> {
              List<PreviousDecisionDTO> previousDecisionDTOs =
                  documentUnit.previousDecisions().stream()
                      .map(
                          previousDecision ->
                              PreviousDecisionDTO.builder()
                                  .id(previousDecision.id())
                                  .documentUnitId(documentUnitDTO.getId())
                                  .decisionDate(previousDecision.date())
                                  .fileNumber(previousDecision.fileNumber())
                                  .courtType(previousDecision.courtType())
                                  .courtLocation(previousDecision.courtPlace())
                                  .build())
                      .toList();

              documentUnitDTO.setPreviousDecisions(previousDecisionDTOs);

              return previousDecisionRepository
                  .saveAll(documentUnitDTO.getPreviousDecisions())
                  .collectList()
                  .map(v -> documentUnitDTO);
            })
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build());
  }

  @Override
  public Mono<DocumentUnit> attachFile(
      UUID documentUnitUuid, String fileUuid, String type, String fileName) {
    return repository
        .findByUuid(documentUnitUuid)
        .map(
            documentUnitDTO -> {
              documentUnitDTO.setS3path(fileUuid);
              documentUnitDTO.setFilename(fileName);
              documentUnitDTO.setFiletype(type);
              documentUnitDTO.setFileuploadtimestamp(Instant.now());

              return documentUnitDTO;
            })
        .flatMap(repository::save)
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build());
  }

  @Override
  public Mono<DocumentUnit> removeFile(UUID documentUnitId) {
    return repository
        .findByUuid(documentUnitId)
        .map(
            documentUnitDTO -> {
              documentUnitDTO.setS3path(null);
              documentUnitDTO.setFilename(null);
              documentUnitDTO.setFiletype(null);
              documentUnitDTO.setFileuploadtimestamp(null);

              return documentUnitDTO;
            })
        .flatMap(repository::save)
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build());
  }

  @Override
  public Mono<Void> delete(DocumentUnit documentUnit) {
    return repository.deleteById(documentUnit.id());
  }

  private Mono<DocumentUnitDTO> addPreviousDecisions(DocumentUnitDTO documentUnit) {
    return previousDecisionRepository
        .findAllByDocumentUnitId(documentUnit.getId())
        .collectList()
        .doOnNext(documentUnit::setPreviousDecisions)
        .map(v -> documentUnit);
  }
}
