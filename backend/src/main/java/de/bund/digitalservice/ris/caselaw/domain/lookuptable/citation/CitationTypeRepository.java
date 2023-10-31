package de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CitationTypeRepository {
  CitationType findBySearchStr(String searchString);

  CitationType findAllByOrderByCitationDocumentTypeAsc();
}
