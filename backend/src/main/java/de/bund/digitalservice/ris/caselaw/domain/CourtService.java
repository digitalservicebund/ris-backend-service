package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtBranchLocationRepository;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class CourtService {
  private final CourtRepository courtRepository;
  private final CourtBranchLocationRepository courtBranchLocationRepository;

  public CourtService(
      CourtRepository courtRepository,
      CourtBranchLocationRepository courtBranchLocationRepository) {
    this.courtRepository = courtRepository;
    this.courtBranchLocationRepository = courtBranchLocationRepository;
  }

  @Transactional(readOnly = true)
  public List<Court> getCourts(String searchStr, Integer size) {
    if (searchStr != null && !searchStr.trim().isBlank()) {
      return courtRepository.findBySearchStr(searchStr.trim(), size);
    }

    return courtRepository.findAllByOrderByTypeAscLocationAsc(size);
  }

  public List<String> getBranchLocationsForCourt(String courtType, String courtLocation) {
    var court = courtRepository.findByTypeAndLocation(courtType, courtLocation).orElse(null);
    if (court != null) {
      return courtBranchLocationRepository.findAllByCourtId(court.id());
    } else {
      return Collections.emptyList();
    }
  }
}
