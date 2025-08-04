package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.LanguageCodeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.LanguageCode;
import de.bund.digitalservice.ris.caselaw.domain.LanguageCodeRepository;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresLanguageCodeRepositoryImpl implements LanguageCodeRepository {
  private final DatabaseLanguageCodeRepository repository;

  public PostgresLanguageCodeRepositoryImpl(DatabaseLanguageCodeRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<LanguageCode> findAllBySearchStr(String searchStr, Integer size) {
    return repository
        .findLanguageCodeDTOByValueContainsIgnoreCase(searchStr.trim(), Limit.of(size))
        .stream()
        .map(LanguageCodeTransformer::transformToDomain)
        .toList();
  }

  @Override
  public List<LanguageCode> findAll(Integer size) {
    return repository.findAllOrderByValueIgnoreCaseLimit(size).stream()
        .map(LanguageCodeTransformer::transformToDomain)
        .toList();
  }
}
