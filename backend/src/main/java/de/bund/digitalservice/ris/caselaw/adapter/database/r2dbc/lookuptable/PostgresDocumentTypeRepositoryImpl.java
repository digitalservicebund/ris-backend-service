package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentTypeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public class PostgresDocumentTypeRepositoryImpl implements DocumentTypeRepository {
  private final DatabaseDocumentTypeRepository repository;

  public PostgresDocumentTypeRepositoryImpl(DatabaseDocumentTypeRepository repository) {
    this.repository = repository;
  }

  @Override
  public Flux<DocumentType> findCaselawBySearchStr(String searchString) {
    return repository
        .findCaselawBySearchStr(searchString)
        .map(DocumentTypeTransformer::transformDTO);
  }

  @Override
  public Flux<DocumentType> findAllByDocumentTypeOrderByJurisShortcutAscLabelAsc(char shortcut) {
    return repository
        .findAllByDocumentTypeOrderByJurisShortcutAscLabelAsc(shortcut)
        .map(DocumentTypeTransformer::transformDTO);
  }
}
