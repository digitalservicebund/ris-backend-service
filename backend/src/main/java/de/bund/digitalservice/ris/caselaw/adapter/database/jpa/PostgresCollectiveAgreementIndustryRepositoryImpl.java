package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import de.bund.digitalservice.ris.caselaw.adapter.transformer.CollectiveAgreementIndustryTransformer;
import de.bund.digitalservice.ris.caselaw.domain.CollectiveAgreementIndustry;
import de.bund.digitalservice.ris.caselaw.domain.CollectiveAgreementIndustryRepository;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresCollectiveAgreementIndustryRepositoryImpl
    implements CollectiveAgreementIndustryRepository {
  private final DatabaseCollectiveAgreementIndustryRepository repository;

  public PostgresCollectiveAgreementIndustryRepositoryImpl(
      DatabaseCollectiveAgreementIndustryRepository repository) {
    this.repository = repository;
  }

  @Override
  public List<CollectiveAgreementIndustry> findAllBySearchStr(String searchStr, Integer size) {
    return repository
        .findCollectiveAgreementIndustryDTOByValueContainsIgnoreCase(
            searchStr.trim(), Limit.of(size))
        .stream()
        .map(CollectiveAgreementIndustryTransformer::transformToDomain)
        .toList();
  }

  @Override
  public List<CollectiveAgreementIndustry> findAll(Integer size) {
    return repository.findAllOrderByValueIgnoreCaseLimit(size).stream()
        .map(CollectiveAgreementIndustryTransformer::transformToDomain)
        .toList();
  }
}
