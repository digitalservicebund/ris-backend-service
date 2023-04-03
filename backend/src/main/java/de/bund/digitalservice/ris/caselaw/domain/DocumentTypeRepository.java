package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import org.springframework.data.repository.NoRepositoryBean;
import reactor.core.publisher.Flux;

@NoRepositoryBean
public interface DocumentTypeRepository {
  Flux<DocumentType> findCaselawBySearchStr(String searchString);

  Flux<DocumentType> findAllByDocumentTypeOrderByJurisShortcutAscLabelAsc(char shortcut);
}
