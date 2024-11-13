package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DocumentTypeRepository {
  List<DocumentType> findCaselawBySearchStr(String searchString);

  List<DocumentType> findIndependentLiteratureBySearchStr(String searchString);

  List<DocumentType> findAllIndependentLiteratureOrderByAbbreviationAscLabelAsc();

  Optional<DocumentType> findUniqueCaselawBySearchStr(String searchString);

  List<DocumentType> findAllByDocumentTypeOrderByAbbreviationAscLabelAsc(char shortcut);
}
