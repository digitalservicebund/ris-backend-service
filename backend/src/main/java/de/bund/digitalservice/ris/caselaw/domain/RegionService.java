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

  public List<Region> getRegions(String searchStr) {
    if (searchStr != null && !searchStr.trim().isBlank()) {
      return repository.findBySearchStr(searchStr.trim());
    }

    return repository.findAllByOrderByCode();
  }

  public List<Region> getApplicabileRegions(String searchStr) {
    return getRegions(searchStr).stream().filter(Region::applicability).toList();
  }
}
