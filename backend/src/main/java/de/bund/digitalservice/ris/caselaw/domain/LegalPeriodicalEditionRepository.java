package de.bund.digitalservice.ris.caselaw.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

/** Domain repository for legal periodical editions */
@NoRepositoryBean
public interface LegalPeriodicalEditionRepository {

  List<LegalPeriodicalEdition> findAllByLegalPeriodicalId(UUID legalPeriodicalId);

  LegalPeriodicalEdition save(LegalPeriodicalEdition legalPeriodicalEdition);
}
