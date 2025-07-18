package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.LanguageCodeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.LanguageCode;
import de.bund.digitalservice.ris.caselaw.domain.LanguageCodeRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresLanguageCodeRepositoryImpl implements LanguageCodeRepository {
  private final DatabaseLanguageCodeRepository repository;

  public PostgresLanguageCodeRepositoryImpl(DatabaseLanguageCodeRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<LanguageCode> findAllBySearchStr(Optional<String> searchStr) {
    if (searchStr.isPresent() && !searchStr.get().isEmpty()) {
      return repository.findLanguageCodeDTOByValueContains(searchStr.get().trim()).stream()
          .map(LanguageCodeTransformer::transformToDomain)
          .toList();
    }
    return Collections.emptyList();
  }
}
