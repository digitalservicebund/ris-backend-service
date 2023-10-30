package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationStyle;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationStyleRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class LookupTableService {
  private final CitationStyleRepository citationStyleRepository;

  private final DocumentTypeRepository documentTypeRepository;

  public LookupTableService(
      DocumentTypeRepository documentTypeRepository,
      CitationStyleRepository citationStyleRepository) {

    this.documentTypeRepository = documentTypeRepository;
    this.citationStyleRepository = citationStyleRepository;
  }

  public Flux<DocumentType> getCaselawDocumentTypes(Optional<String> searchStr) {
    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      return Flux.fromIterable(
          documentTypeRepository.findCaselawBySearchStr(searchStr.get().trim()));
    }

    return Flux.fromIterable(
        documentTypeRepository.findAllByDocumentTypeOrderByAbbreviationAscLabelAsc('R'));
  }

  public Flux<CitationStyle> getCitationStyles(Optional<String> searchStr) {
    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      return citationStyleRepository.findBySearchStr(searchStr.get().trim());
    }

    return citationStyleRepository.findAllByOrderByCitationDocumentTypeAsc();
  }
}
