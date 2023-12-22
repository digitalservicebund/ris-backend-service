package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CitationTypeRepository {

  List<CitationType> findAllBySearchStr(Optional<String> searchStr);
}
