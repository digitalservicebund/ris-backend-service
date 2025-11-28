package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.CurrencyCodeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.CurrencyCode;
import de.bund.digitalservice.ris.caselaw.domain.CurrencyCodeRepository;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresCurrencyCodeRepositoryImpl implements CurrencyCodeRepository {
  private final DatabaseCurrencyCodeRepository repository;

  public PostgresCurrencyCodeRepositoryImpl(DatabaseCurrencyCodeRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<CurrencyCode> findAllBySearchStr(String searchStr, Integer size) {
    return repository
        .findCurrencyCodeDTOByValueContainsIgnoreCase(searchStr.trim(), Limit.of(size))
        .stream()
        .map(CurrencyCodeTransformer::transformToDomain)
        .toList();
  }

  @Override
  public List<CurrencyCode> findAll(Integer size) {
    return repository.findAllOrderByValueIgnoreCaseLimit(size).stream()
        .map(CurrencyCodeTransformer::transformToDomain)
        .toList();
  }
}
