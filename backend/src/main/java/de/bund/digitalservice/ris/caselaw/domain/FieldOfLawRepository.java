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

  List<FieldOfLaw> findByNormStr(String normStr);

  List<FieldOfLaw> findByNormStrAndSearchTerms(String normStr, String[] searchTerms);

  List<FieldOfLaw> findByIdentifierAndSearchTerms(String identifier, String[] searchTerms);

  List<FieldOfLaw> findByIdentifierAndSearchTermsAndNormStr(
      String identifier, String[] searchTerms, String normStr);

  List<FieldOfLaw> findByIdentifierAndNormStr(String identifier, String normStr);

  List<FieldOfLaw> findByIdentifier(String searchStr, Pageable pageable);
}
