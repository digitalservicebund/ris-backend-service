package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtBranchLocationDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtBranchLocationRepository;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
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
  private final DatabaseCourtBranchLocationRepository databaseCourtBranchLocationRepository;

  public CourtService(
      CourtRepository courtRepository,
      DatabaseCourtBranchLocationRepository databaseCourtBranchLocationRepository) {
    this.courtRepository = courtRepository;
    this.databaseCourtBranchLocationRepository = databaseCourtBranchLocationRepository;
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
      return databaseCourtBranchLocationRepository.findAllByCourtId(court.id()).stream()
          .map(CourtBranchLocationDTO::getValue)
          .toList();
    } else {
      return Collections.emptyList();
    }
  }
}
