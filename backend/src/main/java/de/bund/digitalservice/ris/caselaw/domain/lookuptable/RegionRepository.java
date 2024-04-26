package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface RegionRepository {
  List<Region> findBySearchStr(String searchString);

  List<Region> findAllByOrderByCode();
}
