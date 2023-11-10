package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCitationTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.transformer.CitationTypeTransformer;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CitationTypeService {
  private final DatabaseCitationTypeRepository citationTypeRepository;

  public CitationTypeService(DatabaseCitationTypeRepository citationTypeRepository) {

    this.citationTypeRepository = citationTypeRepository;
  }

  public List<CitationType> getCitationStyles(Optional<String> searchStr) {
    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      return citationTypeRepository.findBySearchStr(searchStr.get().trim()).stream()
          .map(CitationTypeTransformer::transformToDomain)
          .toList();
    }

    return citationTypeRepository.findAllByCitationDocumentCategoryOrderByAbbreviation("R").stream()
        .map(CitationTypeTransformer::transformToDomain)
        .toList();
  }
}
