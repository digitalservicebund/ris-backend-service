package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface LanguageCodeRepository {

  List<LanguageCode> findAllBySearchStr(String searchStr, Integer size);

  List<LanguageCode> findAll(Integer size);
}
