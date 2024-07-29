package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface LegalPeriodicalRepository {

  List<LegalPeriodical> findAllBySearchStr(Optional<String> searchStr);
}
