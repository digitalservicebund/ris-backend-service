package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.DocumentType;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.DocumentTypeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Slf4j
public class LookupTableService {

  private final DocumentTypeRepository repository;

  public LookupTableService(DocumentTypeRepository repository) {
    this.repository = repository;
  }

  public Flux<DocumentType> getDocumentTypes() {
    return repository
        .findAll()
        .map(
            documentTypeDTO ->
                new DocumentType(
                    documentTypeDTO.getId(),
                    documentTypeDTO.getJurisShortcut(),
                    documentTypeDTO.getLabel()));
  }
}
