package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalPeriodical;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class LegalPeriodicalService {
  private final LegalPeriodicalRepository legalPeriodicalRepository;

  public LegalPeriodicalService(LegalPeriodicalRepository legalPeriodicalRepository) {

    this.legalPeriodicalRepository = legalPeriodicalRepository;
  }

  public List<LegalPeriodical> getLegalPeriodicals(Optional<String> searchStr) {
    return legalPeriodicalRepository.findAllBySearchStr(searchStr.map(String::trim));
  }
}
