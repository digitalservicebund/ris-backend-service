package de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation;

import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CitationTypeRepository {
  List<CitationType> findBySearchStr(String searchString);

  List<CitationType> findAllByOrderByCitationDocumentTypeAsc();
}
