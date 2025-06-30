package de.bund.digitalservice.ris.caselaw.domain;

import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface DocumentTypeRepository {
  Optional<DocumentType> findUniqueCaselawBySearchStr(String searchString);

  DocumentType findPendingProceeding();

  List<DocumentType> findDocumentTypesBySearchStrAndCategory(
      String searchStr, DocumentTypeCategory category);

  List<DocumentType> findAllDocumentTypesByCategory(DocumentTypeCategory category);
}
