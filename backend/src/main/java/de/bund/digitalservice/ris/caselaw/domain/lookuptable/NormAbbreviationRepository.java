package de.bund.digitalservice.ris.caselaw.domain.lookuptable;

import java.util.List;
import java.util.UUID;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface NormAbbreviationRepository {
  public NormAbbreviation findById(UUID id);

  List<NormAbbreviation> getNormAbbreviationsStartingWithExact(
      String query, Integer size, Integer page);

  List<NormAbbreviation> findAllContainingOrderByAccuracy(String query, Integer size, Integer page);
}
