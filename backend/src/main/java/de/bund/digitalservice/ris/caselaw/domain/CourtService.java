package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CourtService {
  private final CourtRepository courtRepository;

  public CourtService(CourtRepository courtRepository) {
    this.courtRepository = courtRepository;
  }

  public List<Court> getCourts(String searchStr, Integer size) {
    if (searchStr != null && !searchStr.trim().isBlank()) {
      return courtRepository.findBySearchStr(searchStr.trim(), size);
    }

    return courtRepository.findAllByOrderByTypeAscLocationAsc(size);
  }
}
