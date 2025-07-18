package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface LanguageCodeRepository {

  List<LanguageCode> findAllBySearchStr(Optional<String> searchStr);
}
