package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.fieldoflaw.FieldOfLaw;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FieldOfLawRepository {
  List<FieldOfLaw> getTopLevelNodes();

  List<FieldOfLaw> findAllByParentIdentifierOrderByIdentifierAsc(String identifier);

  FieldOfLaw findTreeByIdentifier(String identifier);

  List<FieldOfLaw> findBySearchTerms(String[] searchTerms);

  List<FieldOfLaw> findByNorm(String normStr);

  List<FieldOfLaw> findByNormAndSearchTerms(String normStr, String[] searchTerms);

  List<FieldOfLaw> findByIdentifierAndSearchTerms(String identifier, String[] searchTerms);

  List<FieldOfLaw> findByIdentifierAndSearchTermsAndNorm(
      String identifier, String[] searchTerms, String norm);

  List<FieldOfLaw> findByIdentifierAndNorm(String identifier, String norm);

  List<FieldOfLaw> findByIdentifier(String searchStr, Pageable pageable);
}
