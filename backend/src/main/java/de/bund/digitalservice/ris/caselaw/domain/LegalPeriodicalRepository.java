package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface LegalPeriodicalRepository {

  List<LegalPeriodical> findAllBySearchStr(Optional<String> searchStr);
}
