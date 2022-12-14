package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Repository
public class DocumentUnitRepositoryImpl implements DocumentUnitRepository {

  private final DatabaseDocumentUnitRepository repository;
  private final FileNumberRepository fileNumberRepository;
  private final DeviatingEcliRepository deviatingEcliRepository;
  private final DeviatingDecisionDateRepository deviatingDecisionDateRepository;

  public DocumentUnitRepositoryImpl(
      DatabaseDocumentUnitRepository repository,
      FileNumberRepository fileNumberRepository,
      DeviatingEcliRepository deviatingEcliRepository,
      DeviatingDecisionDateRepository deviatingDecisionDateRepository) {
    this.repository = repository;
    this.fileNumberRepository = fileNumberRepository;
    this.deviatingEcliRepository = deviatingEcliRepository;
    this.deviatingDecisionDateRepository = deviatingDecisionDateRepository;
  }

  private Mono<DocumentUnitDTO> injectFileNumbers(DocumentUnitDTO documentUnitDTO) {
    return fileNumberRepository
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .collectList()
        .flatMap(
            fileNumbers -> {
              documentUnitDTO.setFileNumbers(
                  fileNumbers.stream()
                      .filter(fileNumberDTO -> !fileNumberDTO.getIsDeviating())
                      .map(FileNumberDTO::getFileNumber)
                      .toList());
              documentUnitDTO.setDeviatingFileNumbers(
                  fileNumbers.stream()
                      .filter(FileNumberDTO::getIsDeviating)
                      .map(FileNumberDTO::getFileNumber)
                      .toList());
              return Mono.just(documentUnitDTO);
            });
  }

  private Mono<DocumentUnitDTO> injectDeviatingEclis(DocumentUnitDTO documentUnitDTO) {
    return deviatingEcliRepository
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .collectList()
        .flatMap(
            deviatingEclis -> {
              documentUnitDTO.setDeviatingEclis(
                  deviatingEclis.stream().map(DeviatingEcliDTO::getEcli).toList());
              return Mono.just(documentUnitDTO);
            });
  }

  private Mono<DocumentUnitDTO> injectDeviatingDecisionDates(DocumentUnitDTO documentUnitDTO) {
    return deviatingDecisionDateRepository
        .findAllByDocumentUnitId(documentUnitDTO.getId())
        .collectList()
        .flatMap(
            deviatingDecisionDateDTOs -> {
              documentUnitDTO.setDeviatingDecisionDates(
                  deviatingDecisionDateDTOs.stream()
                      .map(deviatingDecisionDate -> deviatingDecisionDate.getDecisiondate())
                      .toList());
              return Mono.just(documentUnitDTO);
            });
  }

  @Override
  public Mono<DocumentUnitDTO> findByDocumentnumber(String documentnumber) {
    return repository
        .findByDocumentnumber(documentnumber)
        .flatMap(this::injectFileNumbers)
        .flatMap(this::injectDeviatingEclis)
        .flatMap(this::injectDeviatingDecisionDates);
  }

  @Override
  public Mono<DocumentUnitDTO> findByUuid(UUID uuid) {
    return repository
        .findByUuid(uuid)
        .flatMap(this::injectFileNumbers)
        .flatMap(this::injectDeviatingEclis)
        .flatMap(this::injectDeviatingDecisionDates);
  }

  @Override
  @Transactional(transactionManager = "connectionFactoryTransactionManager")
  public Mono<DocumentUnitDTO> save(DocumentUnitDTO documentUnitDTO) {
    return repository
        .save(documentUnitDTO)
        .flatMap(
            duDTO ->
                fileNumberRepository.deleteAllByDocumentUnitId(duDTO.getId()).thenReturn(duDTO))
        .flatMap(
            duDTO -> {
              if (documentUnitDTO.getFileNumbers() == null) {
                return Mono.just(duDTO);
              }
              return fileNumberRepository
                  .saveAll(
                      documentUnitDTO.getFileNumbers().stream()
                          .map(
                              fn ->
                                  FileNumberDTO.builder()
                                      .documentUnitId(duDTO.getId())
                                      .fileNumber(fn)
                                      .isDeviating(false)
                                      .build())
                          .toList())
                  .collectList()
                  .map(f -> duDTO);
            })
        .flatMap(
            duDTO -> {
              if (documentUnitDTO.getDeviatingFileNumbers() == null) {
                return Mono.just(duDTO);
              }
              return fileNumberRepository
                  .saveAll(
                      documentUnitDTO.getDeviatingFileNumbers().stream()
                          .map(
                              fn ->
                                  FileNumberDTO.builder()
                                      .documentUnitId(duDTO.getId())
                                      .fileNumber(fn)
                                      .isDeviating(true)
                                      .build())
                          .toList())
                  .collectList()
                  .map(f -> duDTO);
            })
        .flatMap(
            duDTO ->
                deviatingEcliRepository.deleteAllByDocumentUnitId(duDTO.getId()).thenReturn(duDTO))
        .flatMap(
            duDTO -> {
              if (documentUnitDTO.getDeviatingEclis() == null) {
                return Mono.just(duDTO);
              }
              return deviatingEcliRepository
                  .saveAll(
                      documentUnitDTO.getDeviatingEclis().stream()
                          .map(
                              deviatingEcli ->
                                  DeviatingEcliDTO.builder()
                                      .documentUnitId(duDTO.getId())
                                      .ecli(deviatingEcli)
                                      .build())
                          .toList())
                  .collectList()
                  .map(f -> duDTO);
            })
        .flatMap(
            duDTO ->
                deviatingDecisionDateRepository
                    .deleteAllByDocumentUnitId(duDTO.getId())
                    .thenReturn(duDTO))
        .flatMap(
            duDTO -> {
              if (documentUnitDTO.getDeviatingDecisionDates() == null) {
                return Mono.just(duDTO);
              }
              return deviatingDecisionDateRepository
                  .saveAll(
                      documentUnitDTO.getDeviatingDecisionDates().stream()
                          .map(
                              deviatingDecisionDate ->
                                  DeviatingDecisionDateDTO.builder()
                                      .documentUnitId(duDTO.getId())
                                      .decisiondate(deviatingDecisionDate)
                                      .build())
                          .toList())
                  .collectList()
                  .map(f -> duDTO);
            });
  }

  @Override
  public Mono<Void> delete(DocumentUnitDTO documentUnitDTO) {
    // CASCADE takes care of deleting the connected entries in the
    // FileNumberRepository and
    // DeviatingEcliRepository
    return repository.delete(documentUnitDTO);
  }
}
