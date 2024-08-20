package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

/** Domain repository for legal periodicals */
@NoRepositoryBean
public interface LegalPeriodicalRepository {
  /**
   * Find all legal periodicals, filtered by an optional search string.
   *
   * @param searchStr
   * @return
   */
  List<LegalPeriodical> findAllBySearchStr(Optional<String> searchStr);
}
