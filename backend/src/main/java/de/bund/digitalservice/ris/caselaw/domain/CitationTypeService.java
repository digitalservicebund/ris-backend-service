package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CitationTypeService {
  private final CitationTypeRepository citationTypeRepository;

  public CitationTypeService(CitationTypeRepository citationTypeRepository) {

    this.citationTypeRepository = citationTypeRepository;
  }

  public List<CitationType> getCitationStyles(Optional<String> searchStr) {
    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      return citationTypeRepository.findAllBySearchStr(searchStr.get().trim());
    }

    return citationTypeRepository.findAllByCitationDocumentCategoryOrderByAbbreviation("R");
  }
}
