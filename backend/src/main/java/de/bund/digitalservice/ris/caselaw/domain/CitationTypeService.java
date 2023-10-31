package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationTypeRepository;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class CitationTypeService {
  private final CitationTypeRepository citationTypeRepository;

  public CitationTypeService(CitationTypeRepository citationTypeRepository) {

    this.citationTypeRepository = citationTypeRepository;
  }

  public Flux<CitationType> getCitationStyles(Optional<String> searchStr) {
    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      return Flux.just(citationTypeRepository.findBySearchStr(searchStr.get().trim()));
    }

    return Flux.just(citationTypeRepository.findAllByOrderByCitationDocumentTypeAsc());
  }
}
