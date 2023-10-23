package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationStyle;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationStyleRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.CourtRepository;
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
  private final CourtRepository courtRepository;

  public LookupTableService(
      DocumentTypeRepository documentTypeRepository,
      CourtRepository courtRepository,
      CitationStyleRepository citationStyleRepository) {

    this.documentTypeRepository = documentTypeRepository;
    this.courtRepository = courtRepository;
    this.citationStyleRepository = citationStyleRepository;
  }

  public Flux<DocumentType> getCaselawDocumentTypes(Optional<String> searchStr) {
    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      return documentTypeRepository.findCaselawBySearchStr(searchStr.get().trim());
    }

    return documentTypeRepository.findAllByDocumentTypeOrderByJurisShortcutAscLabelAsc('R');
  }

  public Flux<Court> getCourts(Optional<String> searchStr) {
    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      return courtRepository.findBySearchStr(searchStr.get().trim());
    }

    return courtRepository.findAllByOrderByCourttypeAscCourtlocationAsc();
  }

  public Flux<CitationStyle> getCitationStyles(Optional<String> searchStr) {
    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      return citationStyleRepository.findBySearchStr(searchStr.get().trim());
    }

    return citationStyleRepository.findAllByOrderByCitationDocumentTypeAsc();
  }
}
