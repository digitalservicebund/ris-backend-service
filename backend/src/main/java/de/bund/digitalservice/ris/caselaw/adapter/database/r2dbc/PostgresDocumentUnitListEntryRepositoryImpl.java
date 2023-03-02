package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitListEntry;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitListEntryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class PostgresDocumentUnitListEntryRepositoryImpl
    implements DocumentUnitListEntryRepository {

  private final DatabaseDocumentUnitListEntryRepository repository;
  private final FileNumberRepository fileNumberRepository;

  public PostgresDocumentUnitListEntryRepositoryImpl(
      DatabaseDocumentUnitListEntryRepository repository,
      FileNumberRepository fileNumberRepository) {
    this.repository = repository;
    this.fileNumberRepository = fileNumberRepository;
  }

  @Override
  public Flux<DocumentUnitListEntry> findAll(Sort sort) {
    return repository
        .findAllByDataSourceLike(sort, DataSource.NEURIS.name())
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
