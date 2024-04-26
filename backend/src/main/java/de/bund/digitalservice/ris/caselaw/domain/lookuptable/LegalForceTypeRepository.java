package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface LegalForceTypeRepository {
  List<LegalForceType> findBySearchStr(String searchString);

  List<LegalForceType> findAllByOrderByAbbreviation();
}
