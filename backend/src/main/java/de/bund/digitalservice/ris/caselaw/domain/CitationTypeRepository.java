package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CitationTypeRepository {

  List<CitationType> findAllBySearchStr(String searchStr);

  List<CitationType> findAllByCitationDocumentCategoryOrderByAbbreviation(
      String documentCategoryLabel);
}
