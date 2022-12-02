package de.bund.digitalservice.ris.caselaw.domain;

import java.util.UUID;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Implementation of the document unit repository under the use of the spring data repository logic.
 */
@Repository
public class DocumentUnitRepositoryImpl implements DocumentUnitRepository {

  private final DatabaseDocumentUnitRepository repository;
  private final FileNumberRepository fileNumberRepository;

  /**
   * Constructor to get the repository singleton instances.
   *
   * @param repository spring data repository for document unit table
   * @param fileNumberRepository spring data repository for file number table
   */
  public DocumentUnitRepositoryImpl(
      DatabaseDocumentUnitRepository repository, FileNumberRepository fileNumberRepository) {
    this.repository = repository;
    this.fileNumberRepository = fileNumberRepository;
  }

  /**
   * Get the document unit by the document number. information.
   *
   * @param documentnumber document number of the searched document unit
   * @return result of the database query, empty mono if no document unit for the document number
   *     exit
   */
  @Override
  public Mono<DocumentUnitDTO> findByDocumentnumber(String documentnumber) {
    return repository.findByDocumentnumber(documentnumber).flatMap(this::injectFileNumbers);
  }

  /**
   * Get the document unit by the uuid.
   *
   * @param uuid uuid of the searched document unit
   * @return result of the database query, empty mono if no document unit for the document number
   *     exit
   */
  @Override
  public Mono<DocumentUnitDTO> findByUuid(UUID uuid) {
    return repository.findByUuid(uuid).flatMap(this::injectFileNumbers);
  }

  /**
   * Save the document unit.
   *
   * @param documentUnitDTO data transfer object of the document unit to save
   * @return saved data transfer object of the document unit
   */
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

  /**
   * Delete the document unit
   *
   * @param documentUnitDTO data transfer object of the document unit to delete
   * @return
   */
  @Override
  public Mono<Void> delete(DocumentUnitDTO documentUnitDTO) {
    // CASCADE takes care of deleting the entry in the FileNumberRepository
    return repository.delete(documentUnitDTO);
  }

  /**
   * Add the (deviating) file number information to the document unit.
   *
   * @param documentUnitDTO data transfer object of the document unit
   * @return enriched data transfer object of the document unit
   */
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
}
