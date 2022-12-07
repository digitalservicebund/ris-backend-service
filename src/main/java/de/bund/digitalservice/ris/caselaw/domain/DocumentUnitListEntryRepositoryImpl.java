package de.bund.digitalservice.ris.caselaw.domain;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class DocumentUnitListEntryRepositoryImpl implements DocumentUnitListEntryRepository {

  private final DatabaseDocumentUnitListEntryRepository repository;
  private final FileNumberRepository fileNumberRepository;

  public DocumentUnitListEntryRepositoryImpl(
      DatabaseDocumentUnitListEntryRepository repository,
      FileNumberRepository fileNumberRepository) {
    this.repository = repository;
    this.fileNumberRepository = fileNumberRepository;
  }

  @Override
  public Flux<DocumentUnitListEntry> findAll(Sort sort) {
    return repository
        .findAll(sort)
        .flatMap(
            documentUnitListEntry ->
                fileNumberRepository
                    .findFirstByDocumentUnitIdAndIsDeviating(documentUnitListEntry.getId(), false)
                    .map(
                        fileNumberDTO -> {
                          documentUnitListEntry.setFileNumber(fileNumberDTO.getFileNumber());
                          return documentUnitListEntry;
                        })
                    .defaultIfEmpty(documentUnitListEntry));
  }
}
