package de.bund.digitalservice.ris.caselaw.adapter.database.jpa;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatabaseRegionRepository extends JpaRepository<RegionDTO, UUID> {

  /**
   * Retrieves a list of regions whose codes or long texts start with the provided search strings,
   * ignoring case.
   *
   * @param searchString1 The first search string used to filter regions by code.
   * @param searchString2 The second search string used to filter regions by long text.
   * @return A list of RegionDTO entities whose codes or long texts start with the given search
   *     strings.
   */
  List<RegionDTO> findAllByCodeStartsWithIgnoreCaseOrLongTextStartsWithIgnoreCase(
      String searchString1, String searchString2);

  /**
   * Retrieves a list of all regions ordered by their codes.
   *
   * @return A list of RegionDTO entities ordered by code.
   */
  List<RegionDTO> findAllByOrderByCode();
}
