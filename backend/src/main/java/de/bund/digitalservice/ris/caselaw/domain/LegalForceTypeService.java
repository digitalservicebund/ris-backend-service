package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.LegalForceTypeRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LegalForceTypeService {
  private final LegalForceTypeRepository repository;

  public LegalForceTypeService(LegalForceTypeRepository repository) {
    this.repository = repository;
  }

  public List<LegalForceType> getLegalForceTypes(String searchStr) {
    if (searchStr != null && !searchStr.trim().isBlank()) {
      return repository.findBySearchStr(searchStr.trim());
    }

    return repository.findAllByOrderByAbbreviation();
  }
}
