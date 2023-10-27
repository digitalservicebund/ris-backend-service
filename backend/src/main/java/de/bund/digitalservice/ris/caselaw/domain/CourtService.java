package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.court.CourtRepository;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CourtService {
  private final CourtRepository courtRepository;

  public CourtService(CourtRepository courtRepository) {
    this.courtRepository = courtRepository;
  }

  public List<Court> getCourts(Optional<String> searchStr) {
    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      return courtRepository.findBySearchStr(searchStr.get().trim());
    }

    return courtRepository.findAllByOrderByTypeAscLocationAsc();
  }
}
