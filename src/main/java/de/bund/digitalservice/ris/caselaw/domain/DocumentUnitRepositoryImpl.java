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

  private DocumentUnitDTO injectFileNumbers(DocumentUnitDTO documentUnitDTO) {
    fileNumberRepository
        .findAllByDocumentUnitIdAndIsDeviating(documentUnitDTO.getId(), false)
        .collectList()
        .subscribe(
            fileNumbers ->
                documentUnitDTO.setFileNumbers(
                    fileNumbers.stream().map(FileNumberDTO::getFileNumber).toList()));
    fileNumberRepository
        .findAllByDocumentUnitIdAndIsDeviating(documentUnitDTO.getId(), true)
        .collectList()
        .subscribe(
            fileNumbers ->
                documentUnitDTO.setDeviatingFileNumbers(
                    fileNumbers.stream().map(FileNumberDTO::getFileNumber).toList()));
    return documentUnitDTO;
  }

  @Override
  public Mono<DocumentUnitDTO> findByDocumentnumber(String documentnumber) {
    return repository.findByDocumentnumber(documentnumber).map(this::injectFileNumbers);
  }

  @Override
  public Mono<DocumentUnitDTO> findByUuid(UUID uuid) {
    return repository.findByUuid(uuid).map(this::injectFileNumbers);
  }

  @Override
  public Mono<DocumentUnitDTO> save(DocumentUnitDTO documentUnitDTO) {
    return repository
        .save(documentUnitDTO)
        .flatMap(
            duDTO ->
                fileNumberRepository
                    .deleteAllByDocumentUnitId(duDTO.getId())
                    .flatMap(v -> Mono.just(duDTO)))
        .flatMap(
            duDTO ->
                fileNumberRepository
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
                    .flatMap(f -> Mono.just(duDTO)))
        .flatMap(
            duDTO ->
                fileNumberRepository
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
                    .flatMap(f -> Mono.just(duDTO)));
  }

  @Override
  public Mono<Void> delete(DocumentUnitDTO documentUnitDTO) {
    // CASCADE takes care of deleting the entry in the FileNumberRepository
    return repository.delete(documentUnitDTO);
  }
}
