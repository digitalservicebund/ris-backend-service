package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnit;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.PreviousDecision;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
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
  @Transactional(transactionManager = "connectionFactoryTransactionManager")
  public Mono<DocumentUnit> save(DocumentUnit documentUnit) {
    return repository
        .findByUuid(documentUnit.uuid())
        .map(documentUnitDTO -> DocumentUnitTransformer.enrichDTO(documentUnitDTO, documentUnit))
        .flatMap(repository::save)
        .flatMap(documentUnitDTO -> savePreviousDecisions(documentUnitDTO, documentUnit))
        .map(
            documentUnitDTO ->
                DocumentUnitBuilder.newInstance().setDocumentUnitDTO(documentUnitDTO).build());
  }

  private Mono<DocumentUnitDTO> savePreviousDecisions(
      DocumentUnitDTO documentUnitDTO, DocumentUnit documentUnit) {
    return previousDecisionRepository
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .collectList()
        .flatMap(
            previousDecisionDTOs -> {
              List<Long> toDeleteIds = new ArrayList<>();
              List<PreviousDecisionDTO> toUpdate = new ArrayList<>();
              List<PreviousDecisionDTO> toInsert = new ArrayList<>();

              List<Long> updateIds = new ArrayList<>();

              if (documentUnit.previousDecisions() == null
                  || documentUnit.previousDecisions().isEmpty()) {
                toDeleteIds.addAll(
                    previousDecisionDTOs.stream().map(PreviousDecisionDTO::getId).toList());
              } else {
                toInsert.addAll(
                    documentUnit.previousDecisions().stream()
                        .filter(previousDecision -> previousDecision.id() == null)
                        .map(
                            previousDecision ->
                                PreviousDecisionTransformer.generateDTO(
                                    previousDecision, documentUnitDTO.getId()))
                        .toList());
                List<PreviousDecision> toUpdateOrToDelete =
                    new ArrayList<>(
                        documentUnit.previousDecisions().stream()
                            .filter(previousDecision -> previousDecision.id() != null)
                            .toList());

                previousDecisionDTOs.forEach(
                    previousDecisionDTO -> {
                      Optional<PreviousDecision> previousDecisionOptional =
                          toUpdateOrToDelete.stream()
                              .filter(
                                  previousDecision ->
                                      previousDecisionDTO.getId().equals(previousDecision.id()))
                              .findFirst();
                      if (previousDecisionOptional.isPresent()) {
                        toUpdate.add(
                            PreviousDecisionTransformer.enrichDTO(
                                previousDecisionDTO, previousDecisionOptional.get()));
                        updateIds.add(previousDecisionDTO.getId());
                        toUpdateOrToDelete.removeIf(
                            previousDecision ->
                                previousDecision.id().equals(previousDecisionDTO.getId()));
                      } else {
                        toDeleteIds.add(previousDecisionDTO.getId());
                      }
                    });

                updateIds.addAll(toUpdateOrToDelete.stream().map(PreviousDecision::id).toList());
              }

              List<PreviousDecisionDTO> savedPreviousDecisions = new ArrayList<>();

              return previousDecisionRepository
                  .findAllById(updateIds)
                  .collectList()
                  .map(
                      updateList -> {
                        updateList.forEach(
                            previousDecisionDTO -> {
                              if (!Objects.equals(
                                  previousDecisionDTO.documentUnitId, documentUnitDTO.getId())) {
                                throw new RuntimeException(
                                    "previous decision not for the right document unit");
                              }
                            });
                        return updateList;
                      })
                  .flatMap(list -> previousDecisionRepository.deleteAllById(toDeleteIds))
                  .then(previousDecisionRepository.saveAll(toUpdate).collectList())
                  .map(
                      updatedPreviousDecisions -> {
                        savedPreviousDecisions.addAll(updatedPreviousDecisions);
                        return documentUnitDTO;
                      })
                  .flatMap(v -> previousDecisionRepository.saveAll(toInsert).collectList())
                  .map(
                      insertedPreviousDecisions -> {
                        savedPreviousDecisions.addAll(insertedPreviousDecisions);
                        return documentUnitDTO;
                      })
                  .map(
                      oldDocumentUnitDTO -> {
                        oldDocumentUnitDTO.setPreviousDecisions(savedPreviousDecisions);
                        return oldDocumentUnitDTO;
                      });
            });
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
    return repository
        .findByUuid(documentUnit.uuid())
        .flatMap(documentUnitDTO -> repository.deleteById(documentUnitDTO.getId()));
  }

  private Mono<DocumentUnitDTO> addPreviousDecisions(DocumentUnitDTO documentUnit) {
    return previousDecisionRepository
        .findAllByDocumentUnitId(documentUnit.getId())
        .collectList()
        .doOnNext(documentUnit::setPreviousDecisions)
        .map(v -> documentUnit);
  }
}
