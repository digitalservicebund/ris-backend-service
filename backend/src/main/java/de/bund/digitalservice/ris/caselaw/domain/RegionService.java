package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.Region;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.RegionRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RegionService {
  private final RegionRepository repository;

  public RegionService(RegionRepository repository) {
    this.repository = repository;
  }

  public List<Region> getApplicableRegions(String searchStr) {
    if (searchStr != null && !searchStr.trim().isBlank()) {
      return repository.findApplicableBySearchStr(searchStr.trim());
    }

    return repository.findAllApplicableByOrderByCode();
  }
}
