package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.RegionRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Service class for managing operations related to Region entities. */
@Service
@Slf4j
public class RegionService {

  private final RegionRepository repository;

  /**
   * Constructs a RegionService with the specified RegionRepository.
   *
   * @param repository The RegionRepository used for accessing region data.
   */
  public RegionService(RegionRepository repository) {
    this.repository = repository;
  }

  /**
   * Retrieves a list of applicable regions based on the provided search string. The 'applicability'
   * of a region is true, when it can be used as a 'Geltungsbereich' for 'Gesetzeskraft'. If the
   * search string is null or empty, this returns all applicable regions ordered by code.
   *
   * @param searchStr The search string used to filter applicable regions.
   * @return A list of Region objects representing applicable regions.
   */
  public List<Region> getApplicableRegions(String searchStr) {
    if (searchStr != null && !searchStr.trim().isBlank()) {
      return repository.findApplicableBySearchStr(searchStr.trim());
    }
    return repository.findAllApplicableByOrderByCode();
  }
}
