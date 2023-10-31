package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationTypeRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
@Deprecated
public class LookupTableService {
  private final CitationTypeRepository citationTypeRepository;

  private final DocumentTypeRepository documentTypeRepository;

  public LookupTableService(
      DocumentTypeRepository documentTypeRepository,
      CitationTypeRepository citationTypeRepository) {

    this.documentTypeRepository = documentTypeRepository;
    this.citationTypeRepository = citationTypeRepository;
  }

  public Flux<DocumentType> getCaselawDocumentTypes(Optional<String> searchStr) {
    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      return Flux.fromIterable(
          documentTypeRepository.findCaselawBySearchStr(searchStr.get().trim()));
    }

    return Flux.fromIterable(
        documentTypeRepository.findAllByDocumentTypeOrderByAbbreviationAscLabelAsc('R'));
  }
}
