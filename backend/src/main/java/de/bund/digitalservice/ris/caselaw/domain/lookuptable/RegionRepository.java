package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

/** Interface for accessing Region data without creating a concrete repository bean. */
@NoRepositoryBean
public interface RegionRepository {

  /**
   * Finds applicable regions based on the provided search string. A region's 'applicability'
   * property is true, if it could be used as a 'Geltungsbereich'.
   *
   * @param searchString The search string used to filter applicable regions.
   * @return A list of Region objects representing applicable regions.
   */
  List<Region> findApplicableBySearchStr(String searchString);

  /**
   * Finds all applicable regions and orders them by code.
   *
   * @return A list of Region objects representing all applicable regions ordered by code.
   */
  List<Region> findAllApplicableByOrderByCode();
}
