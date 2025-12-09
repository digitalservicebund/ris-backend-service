package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CurrencyCodeRepository {

  List<CurrencyCode> findAllBySearchStr(String searchStr, Integer size);

  List<CurrencyCode> findAll(Integer size);
}
