package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.DocumentationOfficeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationOfficeNotExistsException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresDocumentationOfficeRepositoryImpl implements DocumentationOfficeRepository {
  private final DatabaseDocumentationOfficeRepository repository;

  public PostgresDocumentationOfficeRepositoryImpl(
      DatabaseDocumentationOfficeRepository repository) {
    this.repository = repository;
  }

  @Override
  public DocumentationOffice findByUuid(UUID uuid) throws DocumentationOfficeNotExistsException {
    var result =
        repository
            .findById(uuid)
            .orElseThrow(
                () ->
                    new DocumentationOfficeNotExistsException(
                        String.format("The documentation office with id %s doesn't exist.", uuid)));
    return DocumentationOfficeTransformer.transformToDomain(result);
  }

  @Override
  public List<DocumentationOffice> findBySearchStr(String searchStr) {
    return repository.findByAbbreviationStartsWithIgnoreCase(searchStr).stream()
        .map(DocumentationOfficeTransformer::transformToDomain)
        .toList();
  }

  @Override
  public List<DocumentationOffice> findAllOrderByAbbreviationAsc() {
    return repository.findAllByOrderByAbbreviationAsc().stream()
        .map(DocumentationOfficeTransformer::transformToDomain)
        .toList();
  }
}
