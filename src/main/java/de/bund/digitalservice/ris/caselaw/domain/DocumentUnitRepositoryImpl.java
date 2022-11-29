package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class DocumentUnitRepositoryImpl implements DocumentUnitRepository {

  private final DatabaseDocumentUnitRepository repository;
  private final FileNumberRepository fileNumberRepository;

  public DocumentUnitRepositoryImpl(
      DatabaseDocumentUnitRepository repository, FileNumberRepository fileNumberRepository) {
    this.repository = repository;
    this.fileNumberRepository = fileNumberRepository;
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

  @Override
  public Mono<DocumentUnitDTO> findByDocumentnumber(String documentnumber) {
    return repository.findByDocumentnumber(documentnumber).flatMap(this::injectFileNumbers);
  }

  @Override
  public Mono<DocumentUnitDTO> findByUuid(UUID uuid) {
    return repository.findByUuid(uuid).flatMap(this::injectFileNumbers);
  }

  @Override
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
                  .flatMap(f -> Mono.just(duDTO));
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
                  .flatMap(f -> Mono.just(duDTO));
            });
  }

  @Override
  public Mono<Void> delete(DocumentUnitDTO documentUnitDTO) {
    // CASCADE takes care of deleting the entry in the FileNumberRepository
    return repository.delete(documentUnitDTO);
  }
}
