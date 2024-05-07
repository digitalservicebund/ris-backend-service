package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface RegionRepository {
  List<Region> findApplicableBySearchStr(String searchString);

  List<Region> findAllApplicableByOrderByCode();
}
