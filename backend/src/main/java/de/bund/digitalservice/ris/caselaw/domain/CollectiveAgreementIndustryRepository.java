package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CollectiveAgreementIndustryRepository {

  List<CollectiveAgreementIndustry> findAllBySearchStr(String searchStr, Integer size);

  List<CollectiveAgreementIndustry> findAll(Integer size);
}
